class Object {

    fn __class__() {
        return Invokes.getClass(this);
    }

    fn __eq__(other) -> boolean? {
        return this is other;
    }

    fn __hash__() -> int? {
        return Invokes.id(this);
    }

    fn __str__() -> String? {
        return __class__().__name__() + "@" + Invokes.id(this);
    }

    fn __repr__() -> String? {
        return __class__().__name__() + "@" + Invokes.id(this);
    }
}

fn wrapper?(obj) {
    return (not Obj?(obj)) or Wrapper?(obj);
}

class Wrapper {
    const value;

    fn __init__(value) {
        this.value = value;
    }

    fn __str__() {
        return str(value);
    }

    fn __repr__() {
        return __str__();
    }

    fn __add__(other) {
        return wrap(value + wrapNum(other).value);
    }

    contract __add__(wrapper?) -> Wrapper?;


    fn __sub__(other) {
        return wrap(value - wrapNum(other).value);
    }

    contract __sub__(wrapper?) -> Wrapper?;


    fn __mul__(other) {
        return wrap(value * wrapNum(other).value);
    }

    contract __mul__(wrapper?) -> Wrapper?;


    fn __div__(other) {
        return wrap(value / wrapNum(other).value);
    }

    contract __div__(wrapper?) -> Wrapper?;


    fn __mod__(other) {
        return wrap(value % wrapNum(other).value);
    }

    contract __mod__(wrapper?) -> Wrapper?;

    fn __gt__(other) {
        return value > wrapNum(other).value;
    }

    fn __lt__(other) {
        return value < wrapNum(other).value;
    }

    fn __eq__(other) {
        wrappedOther := wrap(other);
        return Wrapper?(wrappedOther) and value == wrappedOther.value;
    }

    fn __ne__(other) {
        return not __eq__(other);
    }
}

class Integer(Wrapper) {
    fn __init__(value) {
        super.__init__(int(value));
    }

    fn __hash__() {
        return value;
    }
}

class Float(Wrapper) {
    fn __init__(value) {
        super.__init__(float(value));
    }

    fn __hash__() {
        return Invokes.floatToIntBits(value);
    }
}

class Boolean(Wrapper) {
    fn __init__(value) {
        super.__init__(boolean(value));
    }

    fn __hash__() {
        return 1 if value else 0;
    }
}

class Character(Wrapper) {
    fn __init__(value) {
        super.__init__(char(value));
    }

    fn __hash__() {
        return int(value);
    }
}

class Byte(Wrapper) {
    fn __init__(value) {
        super.__init__(byte(value));
    }

    fn __hash__() {
        return int(value);
    }
}

class Exception {
    const cause;
    const msg;
    var traceMsg;

    fn __init__(msg=null, cause=null) {
        this.cause = cause;
        this.msg = msg if msg is not null else "";
    }

    fn __str__() {
        return "Exception " + msg;
    }

    fn printStackTrace() {
        printErr(__class__().__name__() + ": " + msg + " ");
        printErr(traceMsg);
    }
}

class AttributeException(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class ArgumentException(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class AssertionError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class ContractError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class IndexError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class InheritanceError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class Interruption(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class InvokeError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class MutationError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class NameError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class NotImplementedError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class NullError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class ParameterException(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class RuntimeSyntaxError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class TypeError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class UnknownTypeError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class Iterator<T> {
    fn __hasNext__() -> boolean? {
        throw new NotImplementedError();
    }

    fn __next__() -> T {
        throw new NotImplementedError();
    }
}

class Iterable<T> {
    fn __iter__() -> Iterator? {
        throw new NotImplementedError();
    }
}

/*
 * Extending this class indicates that the child class has collective elements.
 */
class Collection {
    fn size() -> int {
        throw new NotImplementedError();
    }
}

class ArrayIterator(Iterator) {
    const array;
    const endIndex;
    var index = 0;

    fn __init__(array, endIndex=null) {
        this.array = array;
        this.endIndex = array.length if endIndex is null else endIndex;
    }

    fn __hasNext__() {
        return index < endIndex;
    }

    fn __next__() {
        return array[index++];
    }
}

class RangeIterator(Iterator<int?>) {
    var current;
    const end;
    const step;

    fn __init__(begin, end, step) {
        this.current = begin;
        this.end = end;
        this.step = step;
    }

    fn __hasNext__() {
        if step >= 0 {
            return current < end;
        } else {
            return current > end;
        }
    }

    fn __next__() {
        val := current;
        current += step;
        return val;
    }
}

class List<T>(Iterable<T>, Collection) {
    var array;
    var _size;

    fn __init__(*args) {
        initCapacity := _calculateCapacity(args.length) if args.length > 8 else 8;
        array = new Obj[initCapacity];
        _size = args.length;
        for var i = 0; i < _size; i++ {
            set(i, args[i]);
        }
    }

    fn __getItem__(index) {
        return get(index);
    }

    fn __setItem__(index, value) {
        return set(index, value);
    }

    fn __iter__() {
        return new ArrayIterator(array, _size);
    }

    fn __repr__() {
        return "[" + strJoin(", ",
                             this,
                             lambda s -> cond {
                                 case Collection?(s) -> Object.__str__(s);
                                 default -> repr(s);
                             }) + "]";
    }

    fn __str__() {
        return "[" + strJoin(", ", this, repr) + "]";
    }

    fn append(value: T) {
        set(_size++, value);
        if _size == array.length {
            _expand();
        }
    }

    fn get(index: int?) -> T {
        _checkIndex(index)
        return array[index];
    }

    fn insert(index: int?, value: T) {
        if index == _size {
            append(value);
            return;
        }
        _checkIndex(index);

        for i := _size; i > index; i-- {
            array[i] = array[i - 1];
        }
        _size++;
        set(index, value);

        if _size == array.length {
            _expand();
        }
    }

    fn set(index: int?, value: T) {
        _checkIndex(index)
        wrapper := wrap(value);
        array[index] = wrapper;
    }

    fn size() {
        return _size;
    }

    fn remove(index: int?) {
        _checkIndex(index);
        item := get(index);
        for i := index; i < _size; i++ {
            array[i] = array[i + 1];
        }
        _size--;
        if _size < array.length / 4 {
            _collapse();
        }
    }

    fn toArray(eleType=Object) {
        eleProc := cond {
                       case eleType is Object -> lambda x -> x;
                       case Callable?(eleType) -> eleType;
                   };

        resArray := new eleType[_size];
        for i := 0; i < _size; i++ {
            resArray[i] = eleProc(array[i]);
        }
        return resArray;
    }

    fn _checkIndex(index) {
        if index < 0 or index >= _size {
            throw new IndexError("Index out of list size.");
        }
    }

    fn _expand() {
        newArray := new Obj[array.length * 2];
        for i := 0; i < _size; i++ {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    fn _collapse() {
        newArray := new Obj[array.length / 2];
        for i := 0; i < _size; i++ {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    fn _calculateCapacity(inputSize) {
        logVal := Invokes.log(inputSize) / Invokes.log(2);
        exp := int(logVal) + 1;
        return int(Invokes.pow(2, exp));
    }
}

class LinkedListNode<T> {
    var value;
    var next = null;
    var prev = null;

    fn __init__(value: T) {
        this.value = value;
    }
}

class LinkedListIterator<T>(Iterator<T>) {
    var node;

    fn __init__(head: LinkedListNode?) {
        this.node = head;
    }

    fn __hasNext__() {
        return node is not null;
    }

    fn __next__() -> T {
        rtn := node;
        node = node.next;
        return rtn.value;
    }
}

class LinkedList<T>(Iterable<T>, Collection) {
    var head = null;
    var tail = null;
    var _size = 0;

    fn __init__() {
    }

    fn __iter__() {
        return new LinkedListIterator<T>(head);
    }

    fn __repr__() {
        return "[" + strJoin(", ",
                             this,
                             lambda s -> cond {
                                 case Collection?(s) -> Object.__str__(s);
                                 default -> repr(s);
                             }) + "]";
    }

    fn __str__() {
        return "[" + strJoin(", ", this, repr) + "]";
    }

    fn append(value: T) {
        node := new LinkedListNode<T>(value);
        node.prev = tail;
        if tail is not null {
            tail.next = node;
        }
        if head is null {
            head = node;
        }
        tail = node;
        _size++;
    }

    fn prepend(value: T) {
        node := new LinkedListNode<T>(value);
        node.next = head;
        if head is not null {
            head.prev = node;
        }
        if tail is null {
            tail = node;
        }
        head = node;
        _size++;
    }

    fn getHead() -> T {
        if _size == 0 {
            throw new IndexError("Cannot get head from empty list");
        }
        return head.value;
    }

    fn getTail() -> T {
        if _size == 0 {
            throw new IndexError("Cannot get tail from empty list");
        }
        return tail.value;
    }

    fn removeFirst() -> T {
        if _size == 0 {
            throw new IndexError("Cannot remove from empty list");
        }
        _size--;
        first := head;
        head = head.next;
        head.prev = null;
        return first.value;
    }

    fn removeLast() -> T {
        if _size == 0 {
            throw new IndexError("Cannot remove from empty list");
        }
        -size--;
        last := tail;
        tail = tail.prev;
        tail.prev = null;
        return last.value;
    }

    fn size() {
        return _size;
    }
}

class Dict<K, V>(Iterable<K>, Collection) {
    fn __getItem__(key) {
        return get(key);
    }

    fn __setItem__(key, value) {
        put(key, value);
    }

    fn __repr__() {
        return "{" + strJoin(", ",
                             this,
                             lambda k -> repr(k) + "=" +
                                 cond {
                                     case Collection?(get(k)) -> Object.__str__(get(k));
                                     default -> repr(get(k));
                                 }) + "}";
    }

    fn __str__() {
        return "{" + strJoin(", ", this, lambda k -> repr(k) + "=" + repr(get(k))) + "}";
    }

    fn contains(key: K) -> boolean? {
        return get(key) is not null;
    }

    fn put(key: K, value: V) {
        throw new NotImplementedError();
    }

    fn get(key: K) -> V or null? {
        throw new NotImplementedError();
    }

    fn size() {
        throw new NotImplementedError();
    }
}

class NaiveDict<K, V>(Dict<K, V>) {
    const keys;
    const values;
    const length;

    fn __init__(keys: Array?, values: Array?) {
        this.keys = keys;
        this.values = values;
        this.length = keys.length;
    }

    fn __iter__() {
        return new ArrayIterator(keys);
    }

    fn get(key: K) -> V or null? {
        for var i = 0; i < length; i++ {
            if (keys[i] == key) {
                return values[i];
            }
        }
        return null;
    }

    fn put(key, value) {
        throw new MutationError("NaiveDict is immutable.");
    }

    fn size() {
        return length;
    }
}

class HashDictIterator<K>(Iterator<K>) {
    const dict;
    var index = 0;
    var looped = 0;
    var node = null;

    fn __init__(dict: HashDict?) {
        this.dict = dict;
    }

    fn __hasNext__() -> boolean? {
        return looped < dict.size();
    }

    fn __next__() -> T {
        if node is null {
            while index < dict.array.length {
                node = dict.array[index++];
                if node is not null {
                    break;
                }
            }
        }
        looped++;
        cur := node;
        node = node.next;
        return unwrap(cur.key);
    }
}

class HashEntry<K, V> {
    var key;
    var value;
    var next = null;

    fn __init__(key: K, value: V) {
        this.key = key;
        this.value = value;
    }
}

class HashDict<K, V>(Dict<K, V>) {
    const loadFactor;
    var eleCount = 0;
    var array;

    fn __init__(initCap: int? = 8, loadFactor: float? = 0.75) {
        this.loadFactor = loadFactor;
        this.array = new HashEntry?[initCap];
    }

    fn __iter__() {
        return new HashDictIterator<K>(this);
    }

    /*
     * Adds a key and its corresponding value to this dict.
     *
     * Do not do this while loop through this dict.
     */
    fn put(key: K, value: V) {
        hashCode := hash(key, array.length);
        entry := array[hashCode];
        if entry is null {
            array[hashCode] = new HashEntry(key, value);
            eleCount++;
        } else {
            found := false;
            node := entry;
            while node is not null {
                if node.key == key {
                    node.value = value;
                    found = true;
                    break;
                }
                node = node.next;
            }
            if not found {
                newNode := new HashEntry(key, value);
                newNode.next = entry;
                array[hashCode] = newNode;
                eleCount++;
            }
        }
        if _getLoad() > loadFactor {
            _expand();
        }
    }

    fn get(key: K) -> V or null? {
        hashCode := hash(key, array.length);
        entry := array[hashCode];
        if entry is null {
            return null;
        }
        node := entry;
        while node is not null {
            if node.key == key {
                return unwrap(node.value);
            }
            node = node.next;
        }
        return null;
    }

    /*
     * Removes a key from this dict.
     *
     * Do not do this while loop through this dict.
     */
    fn remove(key: K) -> V or null? {
        hashCode := hash(key, array.length);
        entry := array[hashCode];
        if entry is null {
            return null;
        }
        rtn := null;
        if entry.key == key {
            // is the head
            array[hashCode] = entry.next;
            rtn = entry.value;
            eleCount--;
        } else {
            node := entry;
            prev := null;
            while node is not null {
                if node.key == key {
                    prev.next = node.next;
                    rtn = node.value;
                    eleCount--;
                }
                prev = node;
                node = node.next;
            }
        }
        return unwrap(rtn);
    }

    fn size() {
        return eleCount;
    }

    /*
     * Returns the actual index in array.
     */
    fn hash(key, capacity: int?) -> int? {
        var code;
        cond {
            case Object?(key) {
                code = key.__hash__();
            } case Obj?(key) {
                code = key.__hash__();
            } default {
                code = wrap(key).__hash__();
            }
        }
        return code % capacity;
    }

    fn _getLoad() {
        return float(eleCount) / array.length;
    }

    fn _expand() {
        newArr := new HashEntry?[array.length * 2];
        for i := 0; i < array.length; i++ {
            oldEntry := array[i];
            if oldEntry is not null {
                node := oldEntry;
                // divide the current link to two links
                index2 := i + array.length;  // index of the other entry
                curEntry := null;
                newEntry := null;
                while node is not null {
                    index := hash(node.key, newArr.length);
                    next := node.next;
                    if index == i {
                        node.next = curEntry;
                        curEntry = node;
                    } else {
                        node.next = newEntry;
                        newEntry = node;
                    }
                    node = next;
                }
                newArr[i] = curEntry;
                newArr[index2] = newEntry;
            }
        }
        this.array = newArr;
    }
}

class TreeEntry<K, V> {
    var key;
    var value;
    var left = null;
    var right = null;
    var height = 1;

    fn __init__(key: K, value: V) {
        this.key = key;
        this.value = value;
    }
}

class TDValueContainer {
    var node = null;
}

/*
 * An implementation of binary search tree.
 *
 * This is an AVL implementation
 */
class TreeDict<K, V>(Dict<K, V>) {
    var root = null;
    var _size = 0;

    fn __init__() {
    }

    fn __iter__() {
        return linearize().__iter__();
    }

    fn put(key: K, value: V) {
        root = _insert(key, value, root);
    }

    fn get(key: K) -> V {
        return _get(key, root);
    }

    fn linearize(reverse: boolean? = false) -> LinkedList? {
        list := new LinkedList<K>();
        _lin(root, list, reverse);
        return list;
    }

    fn maxKey() -> K {
        if root is null {
            return null;
        }
        return _max(root).key;
    }

    fn minKey() -> K {
        if root is null {
            return null;
        }
        return _min(root).key;
    }

    fn remove(key: K) -> V {
        container := new TDValueContainer();
        root = _delete(key, root, container);
        if container.node is null {
            return null;
        }
        return container.node.value;
    }

    fn size() {
        return _size;
    }

    fn printTree() {
        _printNode(root, 0, "n");
    }

    fn _printNode(node, indent, msg) {
        if node is not null {
            print(" " * indent + msg + ": " + str(node.key) + ": " + str(node.value));
            _printNode(node.left, indent + 2, "l");
            _printNode(node.right, indent + 2, "r");
        }
    }

    fn _lin(node, list, reverse) {
        if node is not null {
            _lin(node.left, list, reverse);
            if reverse {
                list.prepend(node.key);
            } else {
                list.append(node.key);
            }
            _lin(node.right, list, reverse);
        }
    }

    fn _max(node) {
        if node.right is null {
            return node;
        }
        return _max(node.right);
    }

    fn _min(node) {
        if node.left is null {
            return node;
        }
        return _min(node.left);
    }

    fn _get(key, node) {
        if node is null {
            return null;
        }
        cond {
            case key < node.key {
                return _get(key, node.left);
            } case key > node.key {
                return _get(key, node.right);
            } default {
                return node.value;
            }
        }
    }

    fn _height(node) {
        if node is null {
            return 0;
        }
        return node.height;
    }

    fn _balanceFactor(node) {
        if node is null {
            return 0;
        }
        return _height(node.right) - _height(node.left);
    }

    /*
     * Insert and returns the new root at this level.
     */
    fn _insert(key, value, node) {
        if node is null {
            _size++;
            return new TreeEntry<K, V>(key, value);
        }
        cond {
            case key < node.key {
                node.left = _insert(key, value, node.left);
            }
            case key > node.key {
                node.right = _insert(key, value, node.right);
            }
            default {
                node.value = value;
                return node;
            }
        }
        leftH := _height(node.left);
        rightH := _height(node.right);
        node.height = 1 + max(leftH, rightH);
        bf := rightH - leftH
        leftBf := _balanceFactor(node.left);
        rightBf := _balanceFactor(node.right);
        if bf < -1 {  // left longer
            if leftBf < 0 {
                return _rotationLL(node);
            }
            if leftBf > 0 {
                return _rotationLR(node);
            }
            // it is impossible that the bf of left child is 0
            throw new IndexError("Unexpected case.");
        }
        if bf > 1 {
            if rightBf < 0 {
                return _rotationRL(node);
            }
            if rightBf > 0 {
                return _rotationRR(node);
            }
            throw new IndexError("Unexpected case.");
        }
        return node;
    }

    fn _delete(key, node, container=null) {
        if node is null {
            return null;
        }
        var ret;
        cond {
            case key < node.key {
                node.left = _delete(key, node.left, container);
                ret = node;
            }
            case key > node.key {
                node.right = _delete(key, node.right, container);
                ret = node;
            }
            default {
                if container is not null {
                    container.node = node;
                }
                cond {
                    case node.left is null {
                        rn := node.right;
                        node.right = null;
                        _size--;
                        if rn is null {
                            return null;
                        }
                        ret = rn;
                    }
                    case node.right is null {
                        ln := node.left;
                        node.left = null;
                        _size--;
                        ret = ln;  // the last case guarantees ln is not null
                    }
                    default {
                        replacement := _min(node.right);  // use the minimum element that is bigger than node
                        replacement.right = _delete(replacement.key, node.right);  // remove that element from its pos
                        replacement.left = node.left;

                        //node.left = null;
                        //node.right = null;

                        ret = replacement;
                    }
                }
            }
        }

        leftH := _height(ret.left);
        rightH := _height(ret.right);
        ret.height = 1 + max(leftH, rightH);
        bf := rightH - leftH
        leftBf := _balanceFactor(ret.left);
        rightBf := _balanceFactor(ret.right);
        if bf < -1 {  // left longer
            if leftBf <= 0 {
                return _rotationLL(ret);
            }
            if leftBf > 0 {
                return _rotationLR(ret);
            }
        }
        if bf > 1 {
            if rightBf < 0 {
                return _rotationRL(ret);
            }
            if rightBf >= 0 {
                return _rotationRR(ret);
            }
        }
        return ret;
    }

    /* The full name of _rotateLL is 'do rotation in the Left-Left case' */

    fn _rotationLL(node) {
        temp := node.left.right;
        newRoot := node.left;
        newRoot.right = node;
        node.left = temp;

        node.height = 1 + max(_height(node.left), _height(node.right));
        newRoot.height = 1 + max(_height(newRoot.left), _height(newRoot.right));

        return newRoot;
    }

    fn _rotationRR(node) {
        temp := node.right.left;
        newRoot := node.right;
        newRoot.left = node;
        node.right = temp;

        node.height = 1 + max(_height(node.left), _height(node.right));
        newRoot.height = 1 + max(_height(newRoot.left), _height(newRoot.right));

        return newRoot;
    }

    fn _rotationLR(node) {
        node.left := _rotationRR(node.left);
        return _rotationLL(node);
    }

    fn _rotationRL(node) {
        node.right := _rotationLL(node.right);
        return _rotationRR(node);
    }
}

class Set<T>(Iterable<T>, Collection) {
    fn __repr__() {
        return "{" + strJoin(", ",
                             this,
                             lambda s -> cond {
                                 case Collection?(s) -> Object.__str__(s);
                                 default -> repr(s);
                             }) + "}";
    }

    fn __str__() {
        return "{" + strJoin(", ", this, repr) + "}";
    }

    fn remove(item: T) -> T or null? {
        throw new NotImplementedError();
    }

    fn contains(key: K) -> boolean? {
        throw new NotImplementedError();
    }

    fn put(item: T) {
        throw new NotImplementedError();
    }
}

class HashSet<T>(Set<T>) {
    const dict;
    const present = new Object();

    fn __init__(initCap: int? = 8, loadFactor: float? = 0.75) {
        dict = new HashDict<T, Object?>(initCap, loadFactor);
    }

    fn __iter__() {
        return dict.__iter__();
    }

    fn contains(key: K) -> boolean? {
        return dict.contains(key);
    }

    fn put(item: T) {
        dict.put(item, present);
    }

    fn remove(item: T) -> T or null? {
        return dict.remove(item);
    }

    fn size() {
        return dict.size();
    }
}

class TreeSet<T>(Set<T>) {
    const dict;
    const present = new Object();

    fn __init__(initCap: int? = 8, loadFactor: float? = 0.75) {
        dict = new TreeDict<T, Object?>();
    }

    fn __iter__() {
        return dict.__iter__();
    }

    fn contains(key: K) -> boolean? {
        return dict.contains(key);
    }

    fn put(item: T) {
        dict.put(item, present);
    }

    fn remove(item: T) -> T or null? {
        return dict.remove(item);
    }

    fn size() {
        return dict.size();
    }
}

class String {
    const __chars__;
    const length;

    fn __init__(charArray) {
        __chars__ = charArray;
        length = charArray.length;
    }

    fn __add__(other) {
        var otherStr = str(other);
        var array = new char[length + otherStr.length];
        var index = 0;
        for ; index < length; index++ {
            array[index] = __chars__[index];
        }
        for ; index < array.length; index++ {
            array[index] = otherStr.__chars__[index - length];
        }
        return new String(array);
    }

    fn __eq__(other) {
        if not String? (other) {
            return false;
        }
        if other.length != length {
            return false;
        }
        for var i = 0; i < length; i++ {
            if __chars__[i] != other.__chars__[i] {
                return false;
            }
        }
        return true;
    }

    fn __getItem__(index: int?) -> char? {
        return __chars__[index];
    }

    fn __mul__(multiplier: int?) -> String? {
        if multiplier < 0 {
            throw new ArgumentException("String multiplication must have non-negative multiplier.");
        }
        destLen := length * multiplier;
        array := new char[destLen];
        for i := 0; i < multiplier; i++ {
            st := i * length;
            for j := 0; j < length; j++ {
                array[st + j] = __chars__[j];
            }
        }
        return new String(array);
    }

    fn __ne__(other) {
        return not __eq__(other);
    }

    fn __hash__() {
        hash := 33;
        for ch in __chars__ {
            hash = hash * 33 + int(ch);
        }
        return hash;
    }

    fn format(*args) -> String? {
        plainList := new List<String?>();
        patternList := new List<String?>();
        curBegin := 0;
        argIndex := 0;
        for i := 0; i < length - 1; i++ {
            c := __chars__[i];
            if c == '%' {
                next := __chars__[i + 1];
                switch next {
                    case 'd' {
                        plainList.append(substring(curBegin, i));
                        curBegin = i + 2;
                        patternList.append(str(int(args[argIndex++])));
                        i++;
                    }
                    case 'f' {
                        plainList.append(substring(curBegin, i));
                        curBegin = i + 2;
                        patternList.append(str(float(args[argIndex++])));
                        i++;
                    }
                    case 's' {
                        plainList.append(substring(curBegin, i));
                        curBegin = i + 2;
                        patternList.append(str(args[argIndex++]));
                        i++;
                    }
                }
            }
        }
        plainList.append(substring(curBegin));
        totalLen := 0;
        for part in plainList {
            totalLen += part.length;
        }
        for part in patternList {
            totalLen += part.length;
        }
        array := new char[totalLen];
        index := 0;
        for i := 0; i < patternList.size(); i++ {
            pla := plainList.get(i);
            pat := patternList.get(i);
            for j := 0; j < pla.length; j++ {
                array[index++] = pla[j];
            }
            for j := 0; j < pat.length; j++ {
                array[index++] = pat[j];
            }
        }
        pla := plainList.get(plainList.size() - 1);
        for j := 0; j < pla.length; j++ {
            array[index++] = pla[j];
        }
        return new String(array);
    }

    fn substring(begin: int?, end: int? or null? = null) -> String? {
        if end is null {
            end = length;
        }
        if begin > end {
            throw new IndexError("End must greater than begin.");
        }
        if begin < 0 {
            throw new IndexError("Negative index.");
        }
        if end > length {
             throw new IndexError("Index out of string length.");
        }
        size := end - begin;
        array := new char[size];
        for i := 0; i < size; i++ {
            array[i] = __chars__[i + begin];
        }
        return new String(array);
    }

    fn toUpper() -> String? {
        arr := new char[length];
        for i := 0; i < length; i++ {
            c := __chars__[i];
            if c >= 'a' and c <= 'z' {
                arr[i] = char(c - 32);
            } else {
                arr[i] = c;
            }
        }
        return new String(arr);
    }

    fn toLower() -> String? {
        arr := new char[length];
        for i := 0; i < length; i++ {
            c := __chars__[i];
            if c >= 'A' and c <= 'Z' {
                arr[i] = char(c + 32);
            } else {
                arr[i] = c;
            }
        }
        return new String(arr);
    }
}

class InputStream {
    fn close() {
    }

    /*
     * Reads one byte from the stream,
     */
    fn readOne() -> int? {
        throw new NotImplementedError();
    }
}

class OutputStream {
    fn close() {
    }

    /*
     * Writes one byte to the stream.
     */
    fn writeOne(b: byte?) {
        throw new NotImplementedError();
    }

    /*
     * Writes all buffered data to the actual stream.
     */
    fn flush() {
        throw new NotImplementedError();
    }
}

class PrintStream(OutputStream) {
    fn print(s, line: boolean? = true) {
        throw new NotImplementedError();
    }

    fn flush() {
    }

    fn writeOne(b: byte?) {
    }
}

/*
 * Native wrapper class of stdout
 */
class NativeOutStream(PrintStream) {
    fn print(s, line: boolean? = true) {
        if line {
            Invokes.println(s);
        } else {
            Invokes.print(s);
        }
    }
}

/*
 * Native wrapper class of stderr
 */
class NativeErrStream(PrintStream) {
    fn print(s, line: boolean? = true) {
        if line {
            Invokes.printlnErr(s);
        } else {
            Invokes.printErr(s);
        }
    }
}

class Reader {
    fn readLine(omitEol: boolean? = false) {
        throw new NotImplementedError();
    }

    fn close() {
    }
}

class StreamReader {
    const fis;
    const bufferSize = 64;
    var buffer = new byte[0];
    var bufferPos = 0;

    fn __init__(inputStream: InputStream?) {
        fis = inputStream;
    }

    /*
     * Reads a line from the text file, or null if reaches the end of the file.
     */
    fn readLine(omitEol: boolean? = false) -> String? or null? {
        notFound := true;
        res := [];
        while notFound {
            for ; bufferPos < buffer.length; bufferPos++ {
                b := buffer[bufferPos];
                if buffer[bufferPos] == '\n' {
                    if not omitEol {
                        res.append(b);
                    }
                    notFound = false;
                    bufferPos++;
                    break;
                } else {
                    res.append(b);
                }
            }
            if notFound {
                if not _fill() {
                    break;
                }
            }
        }
        arr := res.toArray(byte);
        return Invokes.bytesToString(arr) if arr.length > 0 else null;
    }

    fn close() {
        fis.close();
    }

    /*
     * Reads the whole file as one string.
     */
    fn read() -> String? {
        lst := [];
        var s;
        while (s = readLine()) is not null {
            lst.append(s);
        }
        return strJoin("", lst);
    }

    fn _fill() -> boolean? {
        bufferPos = 0;
        buffer = fis.read(bufferSize);
        return buffer.length > 0;
    }
}

/*
 * Native wrapper class of stdin
 */
class NativeReader(Reader) {
    fn readLine(omitEol: boolean? = false) {
        return Invokes.input();
    }
}

fn anyType(_) {
    return true;
}

/*
 * This function returns a boolean function that:
 *     It returns true if and only if the argument is an array and:
 *         1. The array has element type that is exactly 'eleType', or
 *         2. The array is a generic array and has the generic is exactly 'eleType'
 *
 * For example, `array?(Object?)(new String?[2])` returns false although 'String' extends 'Object'.
 */
fn array?(eleType) {
    return fn arrayType(x) {
        return Array?(x) and (x.type is eleType or x.generics is eleType);
    }
}

fn clock() -> int? {
    return Invokes.clock();
}

fn help(obj) {
    if Class?(obj) or Function?(obj) {
        print(obj.__doc__());
    }
}

fn iter?(x) {
    return Array?(x) or Iterable?(x) or Iterator?(x);
}

fn len(x) {
    return cond {
        case Array?(x) -> x.length;
        case List?(x) -> x.size();
        default -> throw new UnknownTypeError();
    }
}

fn orFn(fn1: Callable?, fn2: Callable?) {
    return lambda x -> fn1(x) or fn2(x);
}

fn input(prompt: String?="") {
    print(prompt, line=false);
    return stdin.readLine();
}

fn max(a, b) {
    return a if a > b else b;
}

fn min(a, b) {
    return b if a > b else a;
}

fn print(s, line: boolean? = true) {
    stdout.print(s, line);
}

fn printErr(s, line: boolean? = true) {
    stderr.print(s, line);
}

fn printArray(arr: Array?, line: boolean = true) {
    print("'[", line=false);
    print(strJoin(", ", arr, repr), line=false);
    print("]", line=line);
}

fn range(begin, end, step=1) {
    return new RangeIterator(begin, end, step);
}

fn script(path, *args) {
    return Invokes.script(path, *args);
}

contract script(String?, String?) -> any?;

fn str(obj) {
    return Invokes.string(obj);
}

fn repr(obj) {
    return Invokes.repr(obj);
}

fn sleep(mills: int?) {
    mills = unwrapNum(mills);
    start := clock();
    while clock() - start < mills {  // busy waiting
    }
}

fn strJoin(deliminator: String?, iter: array?(Obj) or Iterable?, processor: Callable? = null) -> String? {
    strIter := iter;
    if processor is not null {
        strIter = new List();
        for part in iter {
            strIter.append(processor(part));
        }
    }
    totalLength := 0;
    for part in strIter {
        if not String?(part) {
            throw new TypeError("strJoin only joins strings.");
        }
        totalLength += part.length + deliminator.length;
    }
    totalLength -= deliminator.length;
    if totalLength <= 0 {
        return "";
    }
    arr := new char[totalLength];
    index := 0;
    for part in strIter {
        for i := 0; i < part.length; i++ {
            arr[index++] = part.__chars__[i];
        }
        if index < totalLength {
            for i := 0; i < deliminator.length; i++ {
                arr[index++] = deliminator.__chars__[i];
            }
        }
    }
    return new String(arr);
}

fn type(obj) {
    return cond {
        case Object?(obj) -> obj.__class__();
        case Obj?(obj) -> Invokes.nativeType(obj);
        case int?(obj) -> int;
        case float?(obj) -> float;
        case char?(obj) -> char;
        case boolean?(obj) -> boolean;
        case byte?(obj) -> byte;
        default -> null;
    }
}

fn void(x) {
    return x is null;
}

/*
 * Wraps any primitive to wrapper. Leave any referenced value unchanged.
 */
fn wrap(value) {
    cond {
        case int?(value) {
            return new Integer(value);
        } case float?(value) {
            return new Float(value);
        } case char?(value) {
            return new Character(value);
        } case byte?(value) {
            return new Byte(value);
        } case boolean?(value) {
            return new Boolean(value);
        } default {
            return value;
        }
    }
}

fn wrapNum(num) {
    v := wrap(num);
    if not Wrapper?(v) {
        throw new TypeError("Cannot wrap non-wrapper object.");
    }
    return v;
}

/*
 * Unwrap wrappers if it is, or returns the value itself.
 */
fn unwrap(value) {
    if Wrapper?(value) {
        return value.value;
    }
    return value;
}

fn unwrapNum(num) {
    cond {
        case Wrapper?(num) {
            return num.value;
        } case Obj?(num) {
            throw new TypeError("Cannot unwrap non-wrapper object.");
        } default {  // primitive
            return num;
        }
    }
}

// Reflections

fn getAttr(obj: Object?, attr: String?) {
    if hasAttr(obj, attr) {
        return Invokes.getAttr(obj, attr);
    } else {
        throw new AttributeException("Object '" + obj + "' does not have attribute '" + attr + "'.");
    }
}

fn hasAttr(obj: Object?, attr: String?) -> boolean? {
    return Invokes.hasStrAttr(obj, attr);
}

fn listAttr(obj: Object? or Class?) -> array?(Obj) {
    if Object?(obj) {
        return listAttr(obj.__class__());
    } else {
        return Invokes.listAttr(obj);
    }
}

fn listMethod(obj: Object? or Class?) -> array?(Obj) {
    if Object?(obj) {
        return listMethod(obj.__class__());
    } else {
        return Invokes.listMethod(obj);
    }
}

fn setAttr(obj: Object?, attr: String?, value) {
    if hasAttr(obj, attr) {
        Invokes.setAttr(obj, attr, value);
    } else {
        throw new AttributeException("Object '" + obj + "' does not have attribute '" + attr + "'.");
    }
}

fn getClassByName(name: String?) {
    if Invokes.hasGlobalName(name) {
        value := Invokes.getGlobalByName(name);
        if Class?(value) {
            return value;
        }
    }
    throw new AttributeException("Name ''" + name + "' does not exist or is not a class.");
}

fn genericDict(obj: Object?) -> Dict? {
    return Invokes.listGenerics(obj);
}

fn setIn(newIn: Reader?) {
    stdin = newIn;
}

fn setOut(out: PrintStream?) {
    stdout = out;
}

fn setErr(err: PrintStream?) {
    stderr = err;
}

// Constants

const copyright = "Copyright (C) Trash Software Studio.";
const NATIVE_ERROR = new Exception();
const INTERRUPTION = new Interruption("User interruption");

// Variables
var stdout = new NativeOutStream();
var stderr = new NativeErrStream();
var stdin = new NativeReader();
