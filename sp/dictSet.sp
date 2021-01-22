
fn main() {
    d := {1="x", 2=1.6};
    print(d);
    s := {1, 2, 3, 8};
    print(s);
    print(genericDict(d));
    print(s.size());

    td := new TreeDict<int?, int?>();
    for i := 1; i < 21; i++ {
        td.put(i, i * 100);
    }
    td.printTree();
    for i := 10; i < 21; i++ {
        td.remove(i);
    }
    td.printTree();

    ss := new TreeSet<int?>();
    for i := 1; i < 21; i++ {
        ss.put(i);
    }
    print(ss);
}