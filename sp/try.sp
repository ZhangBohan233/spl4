fn main() {

    print(123);
    try {
        a := new int[3];
        a[3];
        throw new Exception("xxs");
    } catch NameError? as e {

    } catch IndexError? as e {
        print("index error");
        throw new Exception(cause=e);
    }
}