class FuckPrint(PrintStream) {
    fn print(s, line=true) {
        //Invokes.printlnErr(s);
    }
}

fn main() {
    assert true;
    assert new Boolean(1);
    print(123);
    //s := input("asdasd");
    //print(s);
    setOut(new FuckPrint());
    //a = 3;
    print(33);
    //Invokes.println(44);
}