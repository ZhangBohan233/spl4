fn main() {
    x := new Integer(3);
    y := x << 4;
    print(y);

    z := float("123.426");
    print(z);

    arr := new String?[2];
    print(array?(Object?)(arr));
    print("mississippi".find("ssix"));
    print("asdasdax".split("a"));

    print(Callable.__superclassOf__(NativeType_Function));
}