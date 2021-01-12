class ArithmeticError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

fn random() -> float? {
    const m = (1 << 15) - 1;
    const a = 3;
    var seed = clock();
    for var i = 0; i < 100; i++ {
        seed = seed * a % m;
    }
    return float(seed) / 32768;
}

fn randInt(low: int?, high: int?) -> int? {
    range1 := high - low;
    if range1 <= 0 {
        throw new ArithmeticError("Upper bound must greater than lower bound.");
    }
    r := random() * range1;
    return int(r) + low;
}

fn round(num: float?, digits: int?) -> float? {

}
