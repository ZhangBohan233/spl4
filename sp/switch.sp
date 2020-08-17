fn main() {
    a := 21;
    c := switch a {
        case 1 -> {
            print(1);
            yield 1;
        } case 2 -> {
            print(2);
            yield 2;
        } default -> {
            print(3);
            yield 3;
        }
    }

    b := cond {
        case a == 1 -> {
            yield 3;
        } case a == 21 -> {
            yield 2;
        } default -> {
            yield 1;
        }
    }
    print(b);

    print(1 == 1);
}