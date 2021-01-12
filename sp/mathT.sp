import namespace math
import "bmpFiller.sp"

fn main() {
    dim := 40;
    times := 100;
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