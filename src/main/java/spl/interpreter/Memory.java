package spl.interpreter;

import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splObjects.*;
import spl.util.Configs;
import spl.util.LineFilePos;

import java.util.*;

public class Memory {

//    public static final int INTERVAL = 1;
    private static final int DEFAULT_HEAP_SIZE = Configs.getInt("heapSize", 8192);
    public final DebugAttributes debugs = new DebugAttributes();
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
    private int stackSize;
    private int stackLimit = Configs.getInt("stackLimit", 512);
    private boolean checkContract;
    private int availableHead = 1;

    public Memory() {
        heapSize = DEFAULT_HEAP_SIZE;
        heap = new SplThing[heapSize];
    }

//    public static void main(String[] args) {
//        int[] arr = {0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8};
//        System.arraycopy(arr, 3, arr, 1, 6);
//        System.out.println(Arrays.toString(arr));
//        AvailableList availableList = new AvailableList(16);
//        System.out.println(availableList.findAva(3));
//        System.out.println(availableList);
//        availableList.addAvaSorted(0, 1);
//        System.out.println(availableList);
//        System.out.println(availableList.findAva(2));
//        System.out.println(availableList);
//    }

    public int getHeapSize() {
        return heapSize;
    }

    public int getHeapUsed() {
        return availableHead;
    }

    public int getAvailableSize() {
        return heapSize - availableHead;
    }

    public void pushStack(FunctionEnvironment newCallEnv, LineFilePos lineFile) {
        stackSize++;
        callStack.push(new StackTraceNode(newCallEnv, lineFile));
        if (stackSize > stackLimit) {
            throw new MemoryError("Stack overflow. ");
        }
    }

    public void decreaseStack() {
        stackSize--;
        callStack.pop();
    }

    public Deque<StackTraceNode> getCallStack() {
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

    public void gc(Environment baseEnv) {
//        System.out.println("gc!!!");
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
        this.stackLimit = stackLimit;
    }

    public boolean isCheckContract() {
        return checkContract;
    }

    public void setCheckContract(boolean checkContract) {
        this.checkContract = checkContract;
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

    private class GarbageCollector {

        private final Map<Integer, Set<ReferenceWrapper>> markedRefs = new HashMap<>();

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

                    SplObject obj = get(objAddr);
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

    /**
     * This class creates a wrapper, which is used as the key in hashmap.
     *
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
}
