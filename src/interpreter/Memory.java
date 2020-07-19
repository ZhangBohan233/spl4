package interpreter;

import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.env.InstanceEnvironment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.*;

import java.util.*;

public class Memory {

    public static final int INTERVAL = 1;
    private static final int DEFAULT_HEAP_SIZE = 2048;
    private int heapSize;
    private int stackSize;
    private int stackLimit = 1000;

    private final SplObject[] heap;

    private AvailableList available;
    private final Set<Environment> temporaryEnvs = new HashSet<>();
    private final Set<Pointer> temporaryPointers = new HashSet<>();
    private final Deque<FunctionEnvironment> callStack = new ArrayDeque<>();

    private final GarbageCollector garbageCollector = new GarbageCollector();
    public final DebugAttributes debugs = new DebugAttributes();

    public Memory() {
        heapSize = DEFAULT_HEAP_SIZE;
        heap = new SplObject[heapSize];

        initAvailable();
    }

    public void pushStack(FunctionEnvironment newCallEnv) {
        stackSize++;
        callStack.push(newCallEnv);
        if (stackSize > stackLimit) {
            throw new MemoryError("Stack overflow. ");
        }
    }

    public void decreaseStack() {
        stackSize--;
        callStack.pop();
    }

    private void initAvailable() {
        available = new AvailableList(heapSize);
    }

    public Pointer allocate(int size, Environment env) {
        int ptr = innerAllocate(size);
        if (ptr == -1) {
            if (debugs.printGcTrigger)
                System.out.println("Triggering gc when allocate " + size + " in " + env);
            gc(env);
            ptr = innerAllocate(size);
            if (ptr == -1)
                throw new MemoryError("Cannot allocate size " + size + ": no memory available. ");
        }
        return new Pointer(ptr);
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

    public void set(Pointer ptr, SplObject obj) {
        heap[ptr.getPtr()] = obj;
    }

    public void set(int addr, SplObject obj) {
        heap[addr] = obj;
    }

    public SplObject get(Pointer ptr) {
        return heap[ptr.getPtr()];
    }

    public SplObject get(int addr) {
        return heap[addr];
    }

    public void free(Pointer ptr, int length) {
//        System.out.println(ptr);
//        System.out.println(available);
        available.addAvaNoSort(ptr.getPtr(), length);
        set(ptr, null);
//        System.out.println(available);
    }

    public void addTempEnv(Environment env) {
        temporaryEnvs.add(env);
    }

    public void removeTempEnv(Environment env) {
        temporaryEnvs.remove(env);
    }

    public void addTempPtr(Pointer tv) {
        temporaryPointers.add(tv);
    }

    public void removeTempPtr(Pointer tv) {
        temporaryPointers.remove(tv);
    }

    public void gc(Environment baseEnv) {
        garbageCollector.garbageCollect(baseEnv);
    }

    public Pointer allocateFunction(SplCallable function, Environment env) {
//        System.out.println("Allocating " + function);
        Pointer ptr = allocate(1, env);
        set(ptr, function);
        return ptr;
    }

    public Pointer allocateObject(SplObject object, Environment env) {
        Pointer ptr = allocate(1, env);
        set(ptr, object);
        return ptr;
    }

    public void printMemory() {
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

    public void setHeapSize(int heapSize) {
        this.heapSize = heapSize;
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
            for (FunctionEnvironment env : callStack) {
//            System.out.println("===" + env);
                mark(env);
            }

            // other roots
            for (Environment env : temporaryEnvs) {
                mark(env);
            }

            // temp object roots
            for (Pointer tempPtr : temporaryPointers) {
//                Pointer ptr = (Pointer) typeValue.getValue();
//                PointerType type = (PointerType) typeValue.getType();
                SplObject obj = get(tempPtr);
                markObjectAsUsed(obj, tempPtr.getPtr());
            }

            // sweep
            sweep();

            if (debugs.printGcRes)
                System.out.println("gc done! Available after gc: " + available.availableCount() +
                        ". Time used: " + (System.currentTimeMillis() - beginTime));
        }

        private void initMarks() {
            for (SplObject obj : heap) {
                if (obj != null) obj.clearGcCount();
            }
        }

        private void mark(Environment env) {
            if (env == null) return;

//        System.out.println(env);

            Set<SplElement> attr = env.attributes();
            for (SplElement ele : attr) {
                if (ele instanceof Pointer) {
                    Pointer ptr = (Pointer) ele;

                    // the null case represent those constants which has not been set yet
                    if (ptr != null) {
                        SplObject obj = get(ptr);

//                        PointerType type = (PointerType) tv.getType();
                        markObjectAsUsed(obj, ptr.getPtr());
                    }
                }
            }
            mark(env.outer);
            if (env instanceof FunctionEnvironment) {
//            System.out.println("***" + ((FunctionEnvironment) env).callingEnv);
                mark(((FunctionEnvironment) env).callingEnv);
            }
//        else
//            if (env instanceof InstanceEnvironment) {
//            markGcByEnv(((InstanceEnvironment) env).creationEnvironment);
//        }
        }

        private void markObjectAsUsed(SplObject obj, int objAddr) {
            if (obj == null) return;
            if (obj.isGcMarked()) return;
            obj.incrementGcCount();
//        if (obj.gcCount > 1) return;  // already marked
//        System.out.println(obj);

//            switch (type.getPointerType()) {
//                case PointerType.ARRAY_TYPE:
            if (obj instanceof SplArray) {
                int arrBegin = objAddr + 1;
                SplArray array = (SplArray) obj;
//                Type valueType = ((ArrayType) type).getEleType();
//                if (!valueType.isPrimitive()) {
//                    PointerType valuePtrType = (PointerType) valueType;
                    for (int i = 0; i < array.length; i++) {
                        int p = arrBegin + i;
                        ReadOnlyPrimitiveWrapper ele = (ReadOnlyPrimitiveWrapper) get(p);
                        if (ele != null) {
                            ele.incrementGcCount();
                            SplObject pointed = get((Pointer) ele.value);
                            markObjectAsUsed(pointed, p);
                        }
                    }
//                } else {
//                    for (int i = 0; i < array.length; i++) {
//                        int p = arrBegin + i;
//                        ReadOnlyPrimitiveWrapper ele = (ReadOnlyPrimitiveWrapper) get(p);
//                        if (ele != null) {
//                            ele.incrementGcCount();
//                        }
//                    }
//                }
            } else if (obj instanceof SplModule) {
                SplModule module = (SplModule) obj;
                mark(module.getEnv());
            } else if (obj instanceof Instance) {
                Instance instance = (Instance) obj;
                mark(instance.getEnv());
            }

        }


        private void sweep() {
            available.clear();
            AvailableList.LnkNode curLast = null;
            for (int p = 1; p < heapSize; ++p) {
                SplObject obj = get(p);
                if (obj != null) {
//                if (obj instanceof ReadOnlyPrimitiveWrapper) {
//                    System.out.println(obj.getGcCount());
//                }
                    if (!obj.isGcMarked()) {
                        curLast = available.addLast(p, curLast);
                        set(p, null);
                    }
                } else {
                    curLast = available.addLast(p, curLast);
                }
            }
        }
    }

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

//        void addAvaSorted(int ptr, int intervalsCount) {
//            LnkNode h = head;
//            while (h.next.value < ptr) {
//                h = h.next;
//            }
//            LnkNode insertHead = h.next;
//            for (int i = intervalsCount - 1; i >= 0; --i) {
//                LnkNode n = new LnkNode();
//                n.next = insertHead;
//                n.value = ptr + i * INTERVAL;
//                insertHead = n;
//            }
//            h.next = insertHead;
//            if (insertHead.value >= insertHead.next.value) {
//                throw new MemoryError("Heap memory collision. ");
//            }
//            size+=intervalsCount;
//        }

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

    public static class MemoryError extends SplException {
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
}
