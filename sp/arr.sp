fn main() {
    a := new String?[4];
    a[0] = "sss";
    print(a);

    b := new Obj[2];
    b[1] = wrap(33);

    print(a.type);
    print(array?(Obj)(a));

    c := new (lambda x -> true)[5];
    print(c.type);
}