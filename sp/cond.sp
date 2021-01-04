fn main() {
    a := 5;
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

    var b = cond {
        case a > 16 -> 1;
        case a > 8  -> 2;
        case a > 4  -> {
            print(">4");
            yield 3;
        }
        default     -> 0;
    }

    print(b);
}
