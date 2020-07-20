fn main() {
    var a = 11;
    var b;
    if a - 4 > 8 {
        b = 1;
    } else {
        if a > 7 {
            b = 2;
        } else {
            if a > 6 {
                b = 3;
            } else {
                b = 4;
            }
        }
    }
    return b;
}
