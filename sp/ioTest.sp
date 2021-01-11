import namespace io

fn main() {
    ism := new FileReader("Untitled.sp");

    print(ism.readLine());
    print(ism.readLine());
    print(ism.readLine());
    print(ism.readLine() is null);

    ism.close();
}
