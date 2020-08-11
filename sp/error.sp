fn test(a) {

    if a > 4 {
        throw new IndexException("god");
    } else {
        var b = new int[3];
        b[4] = 5;
    }
}


fn main() {
    //test(5);

    try {
        test(5);
        return 1;
    } catch IndexException? or Exception? as e {
        print("caught!");
    } catch NativeError {
        print("Native error!")
        return 3;
    } finally {
        print("finally!");
    }

    return 0;
}
