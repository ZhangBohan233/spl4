class A {
    var a = 3;

    fn __init__() {
    }

    fn __str__() {
        return "A:" + str(a);
    }

    fn foo(x: int?) -> String? {
        if x > 1 {
            return "great";
        } else {
            throw new Exception("121");
        }
    }
}

fn la(a) {
    if a > 3 {
        return lambda x -> x + a;
    } else {
        throw new Exception("xxx");
    }
}

fn main() {
    a := new A();
    //print(getAttr(a, "a"));
    //setAttr(a, "a", 2);
    //print(new getClassByName("a")());
    //return a.a;
    //return la(2)(5);
    b := a.foo(1).length;
    print(b);
}