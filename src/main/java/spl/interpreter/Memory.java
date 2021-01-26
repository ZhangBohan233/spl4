package spl.interpreter;

import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.env.ThreadEnvironment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splObjects.*;
import spl.util.LineFilePos;

import java.util.*;

public class Memory {

    public final DebugAttributes debugs = new DebugAttributes();
    public final Options options;
    private final SplThing[] heap;
    private final Set<Environment> temporaryEnvs = Collections.synchronizedSet(new HashSet<>());
    /**
     * Pointers that are managed by memory directly, not from environment.
     */
    private final Set<Reference> managedPointers = Collections.synchronizedSet(new HashSet<>());
    /**
     * Permanent objects that do not collected by garbage collector, such as string literals.
     */
    private final Set<Reference> permanentPointers = Collections.synchronizedSet(new HashSet<>());
    private final Map<Integer, ThreadStack> threadCallStacks = Collections.synchronizedMap(new TreeMap<>());
    /**
     * Function pointers that are marked with keyword 'sync'
     */
    private final Set<Reference> syncPointers = Collections.synchronizedSet(new HashSet<>());
    private final GarbageCollector garbageCollector = new GarbageCollector();
    private final int heapSize;
    private int availableHead = 1;
    private int threadIdAllocator = 1;  // one for the main thread

    public Memory(Options options) {
        this.options = options;

        heapSize = options.getHeapSize();
        heap = new SplThing[heapSize];

        threadCallStacks.put(0, new ThreadStack());  // stack of main thread
    }

    public int getHeapSize() {
        return heapSize;
    }

    public int getHeapUsed() {
        return availableHead;
    }

    public int getAvailableSize() {
        return heapSize - availableHead;
    }

    public synchronized void pushStack(FunctionEnvironment newCallEnv, int threadId, LineFilePos lineFile) {
        ThreadStack threadStack = threadCallStacks.get(threadId);
        if (threadStack == null) {
            throw new MemoryError("No such thread " + threadId);
        }
        threadStack.stackPointer++;
        threadStack.callStack.push(new StackTraceNode(newCallEnv, lineFile));
        if (threadStack.stackPointer > options.getStackLimit()) {
            throw new MemoryError("Stack overflow. ");
        }
    }

    public synchronized void decreaseStack(int threadId) {
        ThreadStack threadStack = threadCallStacks.get(threadId);
        if (threadStack == null) {
            throw new MemoryError("No such thread " + threadId);
        }
        threadStack.stackPointer--;
        threadStack.callStack.pop();
    }

    public synchronized void addSync(Reference ref) {
        syncPointers.add(ref);
    }

    public synchronized void removeSync(Reference ref) {
        syncPointers.remove(ref);
    }

    public synchronized boolean isSynced(Reference ref) {
        return syncPointers.contains(ref);
    }

    public synchronized int newThread() {
        int id = threadIdAllocator++;
        ThreadStack threadStack = new ThreadStack();
        threadCallStacks.put(id, threadStack);
        return id;
    }

    public synchronized void endThread(int threadId) {
        ThreadStack threadStack = threadCallStacks.remove(threadId);
        if (threadStack == null) {
            throw new MemoryError("No such thread " + threadId);
        }
    }

    public synchronized int getThreadPoolSize() {
        return threadCallStacks.size();
    }

    public synchronized Deque<StackTraceNode> getCallStack(int threadId) {
        ThreadStack threadStack = threadCallStacks.get(threadId);
        if (threadStack == null) {
            throw new MemoryError("No such thread " + threadId);
        }
        return threadStack.callStack;
    }

    public synchronized Reference allocate(int size, Environment env) {
        waitGc();
        int ptr = innerAllocate(size);
        if (ptr == -1) {
            if (debugs.printGcTrigger)
                System.out.println("Triggering gc when allocate " + size + " in " + env);
            gc(env);
            ptr = innerAllocate(size);
            if (ptr == -1) {
                throw new MemoryError("Cannot allocate size " + size + ": no memory available. " +
                        "Available memory: " + getAvailableSize() + ". ");
            }
        }
        return new Reference(ptr);
    }

    public synchronized void waitGc() {
        try {
            while (garbageCollector.isInProcess) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int innerAllocate(int size) {
        if (getAvailableSize() >= size) {
            int ptr = availableHead;
            availableHead += size;
            return ptr;
        } else {
            return -1;
        }
    }

    public void set(Reference ptr, SplObject obj) {
        waitGc();
        heap[ptr.getPtr()] = obj;
    }

    public void set(int addr, SplThing obj) {
        waitGc();
        heap[addr] = obj;
    }

    public <T extends SplObject> T get(Reference ptr) {
        waitGc();
        return getNow(ptr);
    }

    @SuppressWarnings("unchecked")
    private <T extends SplObject> T getNow(Reference ptr) {
        return (T) heap[ptr.getPtr()];
    }

    public SplObject get(int addr) {
        waitGc();
        return getNow(addr);
    }

    private SplObject getNow(int addr) {
        return (SplObject) heap[addr];
    }

    public SplThing getRaw(int addr) {
        waitGc();
        return heap[addr];
    }

    public SplElement getPrimitive(int addr) {
        waitGc();
        return getPrimitiveNow(addr);
    }

    private SplElement getPrimitiveNow(int addr) {
        return (SplElement) heap[addr];
    }

    public void addPermanentPtr(Reference ref) {
        permanentPointers.add(ref);
    }

    public void addTempEnv(Environment env) {
        temporaryEnvs.add(env);
    }

    public void removeTempEnv(Environment env) {
        temporaryEnvs.remove(env);
    }

    public void addTempPtr(Reference tv) {
        managedPointers.add(tv);
    }

    public void removeTempPtr(Reference tv) {
        managedPointers.remove(tv);
    }

    public void gc(Environment baseEnv) {
        waitGc();
        garbageCollector.garbageCollect(baseEnv);
    }

    public Reference allocateFunction(SplCallable function, Environment env) {
        Reference ptr = allocate(1, env);
        set(ptr, function);
        return ptr;
    }

    public Reference allocateObject(SplObject object, Environment env) {
        Reference ptr = allocate(1, env);
        set(ptr, object);
        return ptr;
    }

    public void printMemory() {
        System.out.println("Heap used: " + getHeapUsed());
        System.out.println(Arrays.toString(heap));
    }

    public String memoryViewWithAddress() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < heap.length; ++i) {
            sb.append(i).append(": ").append(heap[i]).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    public String memoryView() {
        return memoryViewWithAddress();
    }

    public String availableView() {
        return String.valueOf(availableHead);
    }

    public void setStackLimit(int stackLimit) {
        options.setStackLimit(stackLimit);
    }

    public boolean isCheckContract() {
        return options.isCheckContract();
    }

    public boolean isCheckAssert() {
        return options.isCheckAssert();
    }

    public static class MemoryError extends NativeError {
        MemoryError(String msg) {
            super(msg);
        }
    }

    public static class DebugAttributes {
        private boolean printGcRes;
        private boolean printGcTrigger;

        public void setPrintGcRes(boolean printGcRes) {
            this.printGcRes = printGcRes;
        }

        public void setPrintGcTrigger(boolean printGcTrigger) {
            this.printGcTrigger = printGcTrigger;
        }
    }

    public static class StackTraceNode {
        public final FunctionEnvironment env;
        public final LineFilePos callLineFile;

        StackTraceNode(FunctionEnvironment env, LineFilePos callLineFile) {
            this.env = env;
            this.callLineFile = callLineFile;
        }
    }

    /**
     * The private call stack for each thread.
     */
    private static class ThreadStack {
        private final Deque<StackTraceNode> callStack = new ArrayDeque<>();
        private int stackPointer = 1;
    }

    /**
     * This class creates a wrapper, which is used as the key in hashmap.
     * <p>
     * This class compares two references by their memory location in java. The only way its {@code equals} returns
     * {@code true} is two references are one.
     */
    private static class ReferenceWrapper {
        private final Reference reference;

        ReferenceWrapper(Reference reference) {
            this.reference = reference;
        }

        @Override
        public boolean equals(Object o) {
            return getClass() == o.getClass() && reference == ((ReferenceWrapper) o).reference;
        }

        @Override
        public int hashCode() {
            return reference != null ? reference.hashCode() : 0;
        }
    }

    public static class Options {
        private final boolean checkContract;
        private final boolean checkAssert;
        private int stackLimit;
        private int heapSize;

        public Options(int stackLimit, int heapSize, boolean checkContract, boolean checkAssert) {
            this.stackLimit = stackLimit;
            this.heapSize = heapSize;
            this.checkContract = checkContract;
            this.checkAssert = checkAssert;
        }

        public int getHeapSize() {
            return heapSize;
        }

        public void setHeapSize(int heapSize) {
            this.heapSize = heapSize;
        }

        public int getStackLimit() {
            return stackLimit;
        }

        public void setStackLimit(int stackLimit) {
            this.stackLimit = stackLimit;
        }

        public boolean isCheckContract() {
            return checkContract;
        }

        public boolean isCheckAssert() {
            return checkAssert;
        }
    }

    private class GarbageCollector {

        private final Map<Integer, Set<ReferenceWrapper>> markedRefs = new HashMap<>();
        private boolean isInProcess = false;

        private void addRef(int addr, Reference ref) {
            Set<ReferenceWrapper> refs = markedRefs.get(addr);
            if (refs != null) {
                refs.add(new ReferenceWrapper(ref));
            } else {
                refs = new HashSet<>();
                refs.add(new ReferenceWrapper(ref));
                markedRefs.put(addr, refs);
            }
        }

        private boolean refAlreadyAdded(int objAddr) {
            return markedRefs.containsKey(objAddr);
        }

        /**
         * Since gc() is synchronized, there is no need to synchronize this method.
         *
         * @param baseEnv the environment where the gc happens
         */
        private void garbageCollect(Environment baseEnv) {
            isInProcess = true;
            try {
                long beginTime = System.currentTimeMillis();
                if (debugs.printGcRes)
                    System.out.println("Doing gc! Available before gc: " + getAvailableSize());

                // set all marks to 0
                initMarks();

                // mark
                // global root
                mark(baseEnv);

                // call stack roots
                for (ThreadStack threadStack : threadCallStacks.values()) {
                    for (StackTraceNode stn : threadStack.callStack) {
                        mark(stn.env);
                    }
                }

                // other roots
                for (Environment env : temporaryEnvs) {
                    mark(env);
                }

                // permanent objects
                for (Reference pr : permanentPointers) {
                    SplObject obj = getNow(pr);
                    markObjectAsUsed(obj, pr.getPtr(), pr);
                }

                // temp object roots
                for (Reference tempPtr : managedPointers) {
                    SplObject obj = getNow(tempPtr);
                    markObjectAsUsed(obj, tempPtr.getPtr(), tempPtr);
                }

                // sweep
                sweep();

                if (debugs.printGcRes)
                    System.out.println("gc done! Available after gc: " + getAvailableSize() +
                            ". Time used: " + (System.currentTimeMillis() - beginTime));
            } finally {
                isInProcess = false;
            }
        }

        private void initMarks() {
            markedRefs.clear();
        }

        private void mark(Environment env) {
            if (env == null) return;

            Set<SplElement> attr = env.attributes();
            for (SplElement ele : attr) {
                if (ele instanceof Reference) {
                    Reference ptr = (Reference) ele;

                    // the null case represent those constants which has not been set yet
                    SplObject obj = getNow(ptr);
                    markObjectAsUsed(obj, ptr.getPtr(), ptr);
                }
            }
            mark(env.outer);
            if (env instanceof FunctionEnvironment) {
                mark(((FunctionEnvironment) env).callingEnv);
            }
        }

        private void markObjectAsUsed(SplObject obj, int objAddr, Reference ref) {
            if (obj == null) return;
            if (refAlreadyAdded(objAddr)) return;

            if (ref != null) {
                addRef(objAddr, ref);
            }

            if (obj instanceof SplArray) {
                int arrBegin = objAddr + 1;
                SplArray array = (SplArray) obj;
                for (int i = 0; i < array.length.value; i++) {
                    int p = arrBegin + i;
                    SplElement ele = getPrimitiveNow(p);
                    if (ele instanceof Reference) {
                        // Object[] stores reference as array element, they should also be retargeted
                        Reference refInArray = (Reference) ele;
                        SplObject pointed = getNow(refInArray);
                        markObjectAsUsed(pointed, p, null);
                        addRef(refInArray.getPtr(), refInArray);
                    }
                }
            } else if (obj instanceof SplModule) {
                SplModule module = (SplModule) obj;
                mark(module.getEnv());
            } else if (obj instanceof Instance) {
                Instance instance = (Instance) obj;
                mark(instance.getEnv());
            }

            List<Reference> attrRefs = obj.listAttrReferences();
            if (attrRefs != null) {
                for (Reference attrRef : attrRefs) {
                    if (attrRef != null) addRef(attrRef.getPtr(), attrRef);
                }
            }
        }

        private void sweep() {
            int curAddr = 1;
            for (int p = 1; p < heapSize; p++) {
                Set<ReferenceWrapper> refs = markedRefs.get(p);
                if (refs != null) {
                    int newAddr = curAddr++;
                    int objAddr = 0;
                    Reference firstRef = null;
                    for (ReferenceWrapper refW : refs) {
                        Reference ref = refW.reference;
                        if (firstRef == null) {
                            firstRef = ref;
                            objAddr = firstRef.getPtr();
                        }
                        ref.setPtr(newAddr);
                    }

                    SplObject obj = getNow(objAddr);
                    heap[newAddr] = heap[objAddr];
                    if (obj instanceof SplArray) {
                        SplArray array = (SplArray) obj;
                        // avoid using System.arraycopy() because this may overlap
                        for (int i = 0; i < array.length.value; i++) {
                            heap[curAddr++] = heap[objAddr + i + 1];
                        }
                    }

                }
            }
            availableHead = curAddr;
        }
    }
}
