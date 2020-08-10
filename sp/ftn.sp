import namespace function

fn add(a, b, c) {
    return a + b + c;
}

fn main() {
    g := new Integer(3);
    print(g + 2);
    print( add(*[1, 2], 3));
    b := function.foldl(add, -5, [1, 2, 3, 4, 5], [2, 2, 2, 2, 1]);
    c := function.map(add, [1, 2, 3, 4], [2, 3, 4, 5], [3, 4, 5, 6]);
    print(b);
    print(c);
    print(any(lambda x -> 5 > x, [4, 6, 7, 8]));
}