import io

fn main() {
    file := io.open("Untitled.sp", "r");

    s := file.read();
    print(s);

    file.close();
}
