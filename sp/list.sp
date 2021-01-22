class A {
    fn foo() {
        return this.bar();
    }

    fn bar() {
        return 1;
    }
}

fn main() {
    lst := [1, 2, 3, 4, 5, 6, 7];
    print(lst);

    lst.remove(0);
    print(lst);

    for var i in range(10, 20) {
        lst.insert(1, i);
    }
    lst.append("xs");
    print(lst);

    lst2 := new List<int?>();
    lst2.append(33);
    print(lst2);

    ll := new LinkedList<int?>();
    for var i in range(0, 10) {
        ll.append(i);
    }
    print(ll);
}