import function

class Klass {
    fn fuck() {
        d := new getClass()();
        print(d);
    }

    fn __str__() {
        return "ss";
    }
}

fn add(x, y, z) {
    return x + float(y) / z;
}

fn main() {
    //b := function.foldl(add, 0, [1, 2, 3, 4], [2, 2, 2, 2]);
    print(add(1, *[12, 5]));

    c := new Klass();
    c.fuck();
}