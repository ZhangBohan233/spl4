fn main() {
    a := 9;
    cond {
        case a > 16 {
            print(1);
        } case a > 8 {
            print(2);
            fallthrough;
        } case a > 4 {
            print(3);
            fallthrough;
        } default {
            print(0);
        }
    }
}