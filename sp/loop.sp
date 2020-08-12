
fn main() {
    for var i = 0; i < 10; i++ {
        Invokes.print(i);
    }

    var arr = new int[3];
    arr[0] = 1;
    arr[1] = 2;
    arr[2] = 3;

    for i in arr {
        print(i);
    }

    for i in [3, 4, 5, 6, 7] {
        print(i);
    }

    for i in range(5, 0, -1) {
        print(i);
    }
}