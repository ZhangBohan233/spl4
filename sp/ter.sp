fn main() {
    var a;
    if true {
        a = 3;
    } else {
        a = 2;
    }

    var b = 2 - 1 if a == 1 else 2 * (3 + 2);
    return b;
}