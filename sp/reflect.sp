var glo = 123;

class A {
    var a = 3;

    fn __init__() {
    }

    fn __str__() {
        return "A:" + str(a);
    }

    fn foo(x) {
        if x > 1 {
            return "great";
        } else {
            throw new Exception("121");
        }
    }

    fn bar(x) {
        return x + glo;
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
    print(A.bar(a, 2));
}