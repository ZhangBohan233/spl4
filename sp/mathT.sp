import namespace math
import "bmpFiller.sp"

fn testRand() {
    dim := 40;
    times := 400;
    arr2d := new Object[dim];
    for i := 0; i < dim; i++ {
        arr2d[i] = new boolean[dim];
    }
    for i := 0; i < times; i++ {
        y := math.randInt(0, dim);
        x := math.randInt(0, dim);
        arr2d[y][x] = true;
    }
    bmpFiller.fill(arr2d);
}

fn main() {
    print(math.round(3.1415926535, 2));
    print(pow(10, 2.5));
    print(log2(8));
    print(fact(1));
    print(isNaN(pow(-2, 2.5)));
    print(cos(0.5));
}