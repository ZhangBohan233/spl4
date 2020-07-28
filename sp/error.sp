fn test(a) {

    if a > 4 {
        throw new IndexException("god");
    }
}


fn main() {
    test(5);

    try {
        test(5);
    } catch IndexException or Exception as e {

    } catch Error {

    } finally {

    }

    return 1;
}
