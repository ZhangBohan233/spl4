@Deprecated
@Override
fn test() {

}

@Deprecated
class X {

}

fn main() {
    print(test.__annotations__);
    x := new X();
    print(x.__class__().__annotations__);
}