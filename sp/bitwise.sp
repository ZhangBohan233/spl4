fn main() {
    a := 20;
    b := a & -a;
    print(b);
    c := byte(12b) + 1;
    print(int?(c & 11));
    arr := new byte[6];
    arr[0] = 34b;
    arr[5] = 356b;
    print(char(arr[5]));

    print(type(Invokes));
}