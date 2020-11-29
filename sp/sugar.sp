
fn foo(a: int?, b: float? = 2.5) -> int? {
    return a + int(b);
}

fn main() {
    var e = foo(2);
    return e;
}