package spl.interpreter;

import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
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
    private final Set<Environment> temporaryEnvs = new HashSet<>();
    /**
     * Pointers that are managed by memory directly, not from environment.
     */
    private final Set<Reference> managedPointers = new HashSet<>();
    /**
     * Permanent objects that do not collected by garbage collector, such as string literals.
     */
    private final Set<Reference> permanentPointers = new HashSet<>();
    private final Deque<StackTraceNode> callStack = new ArrayDeque<>();
    private final GarbageCollector garbageCollector = new GarbageCollector();
    private final int heapSize;
    private int stackPointer;
    private int availableHead = 1;
    private long totalGcTime = 0;

    public Memory(Options options) {
        this.options = options;

        heapSize = options.getHeapSize();
        heap = new SplThing[heapSize];
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

    public synchronized void pushStack(FunctionEnvironment newCallEnv, LineFilePos lineFile) {
        stackPointer++;
        callStack.push(new StackTraceNode(newCallEnv, lineFile));
        if (stackPointer > options.getStackLimit()) {
            throw new MemoryError("Stack overflow. ");
        }
    }

    public synchronized void decreaseStack() {
        stackPointer--;
        callStack.pop();
    }

    public synchronized Deque<StackTraceNode> getCallStack() {
        return callStack;
    }

    public Reference allocate(int size, Environment env) {
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
        heap[ptr.getPtr()] = obj;
    }

    public void set(int addr, SplThing obj) {
        heap[addr] = obj;
    }

    @SuppressWarnings("unchecked")
    public <T extends SplObject> T get(Reference ptr) {
        return (T) heap[ptr.getPtr()];
    }

    public SplObject get(int addr) {
        return (SplObject) heap[addr];
    }

    public SplThing getRaw(int addr) {
        return heap[addr];
    }

    public SplElement getPrimitive(int addr) {
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

    public synchronized void gc(Environment baseEnv) {
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

    public long getTotalGcTime() {
        return totalGcTime;
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

        private final Map<Integer, Reference> markedRefs = new HashMap<>();

        private void addRef(int addr, Reference ref) {
            Reference addedRef = markedRefs.get(addr);
            if (addedRef != null) {
                if (addedRef != ref) {
                    throw new MemoryError("Multiple references point to same address.");
                }
            } else {
                markedRefs.put(addr, ref);
            }
        }

        private boolean refAlreadyAdded(int objAddr) {
            return markedRefs.containsKey(objAddr);
        }

        private void garbageCollect(Environment baseEnv) {
            long beginTime = System.currentTimeMillis();
            if (debugs.printGcRes)
                System.out.println("Doing gc! Available before gc: " + getAvailableSize());

            // set all marks to 0
            initMarks();

            // mark
            // global root
            mark(baseEnv);

            // call stack roots
            for (StackTraceNode stn : callStack) {
                mark(stn.env);
            }

            // other roots
            for (Environment env : temporaryEnvs) {
                mark(env);
            }

            // permanent objects
            for (Reference pr : permanentPointers) {
                SplObject obj = get(pr);
                markObjectAsUsed(obj, pr.getPtr(), pr);
            }

            // temp object roots
            for (Reference tempPtr : managedPointers) {
                SplObject obj = get(tempPtr);
                markObjectAsUsed(obj, tempPtr.getPtr(), tempPtr);
            }

            // sweep
            sweep();

            totalGcTime += (System.currentTimeMillis() - beginTime);
            if (debugs.printGcRes)
                System.out.println("gc done! Available after gc: " + getAvailableSize() +
                        ". Time used: " + (System.currentTimeMillis() - beginTime));
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
                    SplObject obj = get(ptr);
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
            if (objAddr == 0) return;
            if (refAlreadyAdded(objAddr)) return;

            if (ref != null) {
                addRef(objAddr, ref);
            }

            if (obj instanceof SplArray) {
                int arrBegin = objAddr + 1;
                SplArray array = (SplArray) obj;
                for (int i = 0; i < array.length.value; i++) {
                    int p = arrBegin + i;
                    SplElement ele = getPrimitive(p);
                    if (ele instanceof Reference) {
                        // Object[] stores reference as array element, they should also be retargeted
                        Reference refInArray = (Reference) ele;
                        SplObject pointed = get(refInArray);
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
                    if (attrRef != null) {
                        markObjectAsUsed(get(attrRef), attrRef.getPtr(), attrRef);
                    }
                }
            }
        }

        private void sweep() {
            int curAddr = 1;
            for (int p = 1; p < heapSize; p++) {
                Reference ref = markedRefs.get(p);
                if (ref != null) {
                    int newAddr = curAddr++;
                    ref.setPtr(newAddr);

                    SplObject obj = get(p);
                    heap[newAddr] = heap[p];
                    if (obj instanceof SplArray) {
                        SplArray array = (SplArray) obj;
                        // avoid using System.arraycopy() because that may overlap
                        for (int i = 0; i < array.length.value; i++) {
                            heap[curAddr++] = heap[++p];
                        }
                    }
                }
            }
            availableHead = curAddr;
        }
    }
}
