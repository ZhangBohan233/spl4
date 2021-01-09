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

    public static final int INTERVAL = 1;
    private static final int DEFAULT_HEAP_SIZE = 1024;
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
    private int heapSize;
    private int stackSize;
    private int stackLimit = 1000;
    private boolean checkContract = true;
    private AvailableList available;

    public Memory() {
        heapSize = DEFAULT_HEAP_SIZE;
        heap = new SplThing[heapSize];

        initAvailable();
    }

    public int getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(int heapSize) {
        this.heapSize = heapSize;
    }

    public int getHeapUsed() {
        return heapSize - available.size;
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

    private void initAvailable() {
        available = new AvailableList(heapSize);
    }

    public Reference allocate(int size, Environment env) {
        int ptr = innerAllocate(size);
        if (ptr == -1) {
            if (debugs.printGcTrigger)
                System.out.println("Triggering gc when allocate " + size + " in " + env);
            gc(env);
            ptr = innerAllocate(size);
            if (ptr == -1) {
                if (available.size >= size) {
                    reallocate();
                    ptr = innerAllocate(size);
                    if (ptr > 0) return new Reference(ptr);
                }
                throw new MemoryError("Cannot allocate size " + size + ": no memory available. " +
                        "Available memory: " + available.availableCount() + ". ");

            }
        }
        return new Reference(ptr);
    }

    private int innerAllocate(int size) {
        int ptr;
        if (size == 1) {
            ptr = available.firstAva();
        } else {
            ptr = available.findAva(size);
        }
        return ptr;
    }

    /**
     * Rearrange memory to remove fragile memory.
     */
    private void reallocate() {
        MemoryRearranger mr = new MemoryRearranger();
        mr.rearrange();
    }

    public void set(Reference ptr, SplObject obj) {
        heap[ptr.getPtr()] = obj;
    }

    public void set(int addr, SplThing obj) {
        heap[addr] = obj;
    }

    public SplObject get(Reference ptr) {
        return (SplObject) heap[ptr.getPtr()];
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

    public void free(Reference ptr, int length) {
//        System.out.println(ptr);
//        System.out.println(available);
        available.addAvaNoSort(ptr.getPtr(), length);
        set(ptr, null);
//        System.out.println(available);
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
        System.out.println("gc!!!");
        garbageCollector.garbageCollect(baseEnv);
    }

    public Reference allocateFunction(SplCallable function, Environment env) {
//        System.out.println("Allocating " + function);
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
        System.out.println("Heap used: " + (heapSize - available.size));
        System.out.println(Arrays.toString(heap));
    }

    public String memoryViewWithAddress() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < heap.length; ++i) {
            sb.append(i).append(": ").append(get(i)).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    public String memoryView() {
//        return Arrays.toString(heap);
        return memoryViewWithAddress();
    }

    public String availableView() {
        return available.availableCount() + ": " + available.toString();
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

    //    public static void main(String[] args) {
//        AvailableList availableList = new AvailableList(16);
//        System.out.println(availableList.findAva(3));
//        System.out.println(availableList);
//        availableList.addAvaSorted(0, 1);
//        System.out.println(availableList);
//        System.out.println(availableList.findAva(2));
//        System.out.println(availableList);
//    }

    private static class AvailableList {

        private final LnkNode head;

        /**
         * this size includes the head (0, null), which would never be used.
         */
        private int size;

        AvailableList(int size) {
            LnkNode last = null;
            for (int i = size - 1; i >= 0; --i) {
                LnkNode node = new LnkNode();
                node.next = last;
                node.value = i;
                last = node;
            }
            head = last;
            this.size = size;
        }

        void clear() {
            head.next = null;
            size = 1;
        }

        /**
         * Add last and returns the temp last
         *
         * @param addr    addr to add
         * @param curLast the current last node, null if it is the head (fixed)
         * @return the current last node
         */
        LnkNode addLast(int addr, LnkNode curLast) {
            LnkNode node = new LnkNode();
            node.value = addr;
            if (curLast == null) curLast = head;

            size++;

            curLast.next = node;
            return node;
        }

        int firstAva() {
            LnkNode node = head.next;
            if (node == null) return -1;
            head.next = node.next;
            size--;
            return node.value;
        }

        void addAvaNoSort(int ptr, int intervalsCount) {
            LnkNode last = new LnkNode();
            LnkNode firstOfAdd = last;
            last.value = ptr;
            for (int i = 1; i < intervalsCount; ++i) {
                LnkNode n = new LnkNode();
                n.value = ptr + i * INTERVAL;
                last.next = n;
                last = n;
            }
            last.next = head.next;
            head.next = firstOfAdd;
            size += intervalsCount;
        }

        int findAva(int size) {
            LnkNode h = head;
            while (h.next != null) {
                int i = 0;
                LnkNode cur = h.next;
                for (; i < size - 1; ++i) {
                    LnkNode next = cur.next;
                    if (next == null || next.value != cur.value + INTERVAL) break;
                    cur = next;
                }
                if (i == size - 1) {
                    LnkNode foundNode = h.next;
                    int found = foundNode.value;
                    h.next = cur.next;
                    this.size -= size;
                    return found;
                } else {
                    h = cur;
                }
            }
            return -1;
        }

        /**
         * @return count of available memory slots, does not include the head.
         */
        public int availableCount() {
            int s = 0;
            for (LnkNode n = head; n != null; n = n.next) {
                s++;
            }
            if (s != size) throw new IndexOutOfBoundsException("Expect count: " + size + ", actual count: " + s);
            return s - 1;  // exclude null
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("Ava[");
            for (LnkNode n = head; n != null; n = n.next) {
                stringBuilder.append(n.value).append("->");
            }
            stringBuilder.append("]");
            return stringBuilder.toString();
        }

        private static class LnkNode {
            LnkNode next;
            int value;
        }
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

    private class MemoryRearranger {
        private final Map<Integer, Reference> refs = new HashMap<>();

        private void rearrange() {
            refs.clear();
        }

        private void add(Environment env) {
            if (env == null) return;

//            System.out.println(env);

            Set<SplElement> attr = env.attributes();
            for (SplElement ele : attr) {
                if (ele instanceof Reference) {
                    Reference ptr = (Reference) ele;

                    // the null case represent those constants which has not been set yet
//                    SplObject obj = get(ptr);
                    add(ptr);
//                    markObjectAsUsed(obj, ptr.getPtr());
                }
            }
            add(env.outer);
            if (env instanceof FunctionEnvironment) {
                add(((FunctionEnvironment) env).callingEnv);
            }
        }

        private void add(SplObject obj) {

        }

        private void add(Reference reference) {
            refs.put(reference.getPtr(), reference);
            SplObject obj = get(reference);
            if (obj instanceof SplArray) {
                int arrBegin = reference.getPtr() + 1;
                SplArray array = (SplArray) obj;
                for (int i = 0; i < array.length; i++) {
                    int p = arrBegin + i;
                    SplElement ele = getPrimitive(p);
                    if (ele instanceof Reference) {
                        SplObject pointed = get((Reference) ele);
//                        add(pointed, p);
                    }
                }
            } else if (obj instanceof SplModule) {
                SplModule module = (SplModule) obj;
                add(module.getEnv());
            } else if (obj instanceof Instance) {
                Instance instance = (Instance) obj;
                add(instance.getEnv());
            } else if (obj instanceof SplClass) {
                SplClass clazz = (SplClass) obj;
                List<Reference> classPointers = clazz.getAllAttrPointers();
                for (Reference attrPtr : classPointers) {
//                    SplObject attrObj = get(attrPtr);
                    add(attrPtr);
                }
            }
        }

        private void move() {

        }
    }

    private class GarbageCollector {

        private void garbageCollect(Environment baseEnv) {
            long beginTime = System.currentTimeMillis();
            if (debugs.printGcRes)
                System.out.println("Doing gc! Available before gc: " + available.availableCount());

            // set all marks to 0
            initMarks();

            // mark
            // global root
            mark(baseEnv);

            // call stack roots
            for (StackTraceNode stn : callStack) {
//            System.out.println("===" + env);
                mark(stn.env);
            }

            // other roots
            for (Environment env : temporaryEnvs) {
                mark(env);
            }

            // permanent objects
            for (Reference pr : permanentPointers) {
                SplObject obj = get(pr);
                markObjectAsUsed(obj, pr.getPtr());
            }

            // temp object roots
            for (Reference tempPtr : managedPointers) {
                SplObject obj = get(tempPtr);
                System.out.println(obj);
                markObjectAsUsed(obj, tempPtr.getPtr());
            }

            // sweep
            sweep();

            if (debugs.printGcRes)
                System.out.println("gc done! Available after gc: " + available.availableCount() +
                        ". Time used: " + (System.currentTimeMillis() - beginTime));
        }

        private void initMarks() {
            for (SplThing obj : heap) {
                if (obj instanceof SplObject)
                    ((SplObject) obj).clearGcCount();
            }
        }

        private void mark(Environment env) {
            if (env == null) return;

//            System.out.println(env);

            Set<SplElement> attr = env.attributes();
            for (SplElement ele : attr) {
                if (ele instanceof Reference) {
                    Reference ptr = (Reference) ele;

                    // the null case represent those constants which has not been set yet
                    SplObject obj = get(ptr);
                    markObjectAsUsed(obj, ptr.getPtr());
                }
            }
            mark(env.outer);
            if (env instanceof FunctionEnvironment) {
                mark(((FunctionEnvironment) env).callingEnv);
            }
        }

        private void markObjectAsUsed(SplObject obj, int objAddr) {
            if (obj == null) return;
            if (obj.isGcMarked()) return;
            obj.incrementGcCount();
//        if (obj.gcCount > 1) return;  // already marked
//        System.out.println(obj);

            if (obj instanceof SplArray) {
                int arrBegin = objAddr + 1;
                SplArray array = (SplArray) obj;
                for (int i = 0; i < array.length; i++) {
                    int p = arrBegin + i;
                    SplElement ele = getPrimitive(p);
                    if (ele instanceof Reference) {
                        SplObject pointed = get((Reference) ele);
                        markObjectAsUsed(pointed, p);
                    }
                }
            } else if (obj instanceof SplModule) {
                SplModule module = (SplModule) obj;
                mark(module.getEnv());
            } else if (obj instanceof Instance) {
                Instance instance = (Instance) obj;
                mark(instance.getEnv());
            } else if (obj instanceof SplClass) {
                SplClass clazz = (SplClass) obj;
                List<Reference> classPointers = clazz.getAllAttrPointers();
                for (Reference attrPtr : classPointers) {
                    SplObject attrObj = get(attrPtr);
                    markObjectAsUsed(attrObj, attrPtr.getPtr());
                }
            }
        }

        private void sweep() {
            available.clear();
            AvailableList.LnkNode curLast = null;
            for (int p = 1; p < heapSize; ++p) {
                SplThing thing = getRaw(p);
                if (thing instanceof SplObject) {
                    SplObject obj = (SplObject) thing;
                    if (obj.isGcMarked()) {
                        if (obj instanceof SplArray) {
                            p += ((SplArray) obj).length;  // do not add any addresses in this array to available list
                        }
                    } else {
                        curLast = available.addLast(p, curLast);
                        set(p, null);  // just for visual clearance, not necessary
                    }
                } else {
                    curLast = available.addLast(p, curLast);
                }
            }
        }
    }
}
