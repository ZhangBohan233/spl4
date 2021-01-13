const E = 2.7182818284590452354;
const PI = 3.14159265358979323846;
const DEG_TO_RAD = 0.017453292519943295;
const RAD_TO_DEG = 57.29577951308232;

const NaN = 0.0 / 0.0;
const LN2 = 0.6931471805599453094;

var expN = 256;
var powN = 21;
var cosN = 32;

class ArithmeticError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

fn intOrFloat?(x) -> boolean? {
    return int?(x) or float?(x);
}

fn abs(x: intOrFloat?) -> intOrFloat? {
    return -x if x < 0 else x;
}

fn exp(power: intOrFloat?) -> float? {
    return pow(1 + power / expN, expN);
}

fn fact(n: int?) -> int? {
    if n < 0 {
        throw new ArithmeticError("Factorial takes non-negative integer as argument.");
    }
    res := 1;
    for i := 1; i <= n; i++ {
        res *= i;
    }
    return res;
}

fn isNaN(x) {
    return x != x;
}

fn logE(x: intOrFloat?) -> float? {
    return Invokes.logE(float(x));
}

fn log2(x: intOrFloat?) -> float? {
    return log(2, x);
}

fn log(base: intOrFloat?, x: intOrFloat?) -> float? {
    return logE(x) / logE(base);
}

fn pow(base: intOrFloat?, expo: intOrFloat?) -> intOrFloat? {
    if expo == 0 {
        return 1;
    }
    posExp := abs(expo);
    if int?(expo) {
        res := 1;
        for i := 0; i < posExp; i++ {
            res *= base;
        }
        return res if expo > 0 else 1.0 / res;
    } else {
        powOf2 := posExp * log2(base);
        // taylor expansion
        res := 1;
        x := powOf2;
        expLn := LN2;
        factN := 1;
        for i := 1; i < powN; i++ {
            factN *= i;
            res += expLn * x / factN;
            x *= powOf2;
            expLn *= LN2;
        }
        return res if expo > 0 else 1.0 / res;
    }
}

var randSeed = 0;

fn random() -> float? {
    const m = (1 << 15) - 1;
    const a = 3;
    var seed = clock() + randSeed;
    for var i = 0; i < 100; i++ {
        seed = seed * a % m;
    }
    randSeed = seed;
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

fn round(num: float?, digits: int? = 0) -> float? or int? {
    if digits == 0 {
        floored := int(num);
        if num - floored < 0.5 {
            return floored;
        } else {
            return floored + 1;
        }
    } else {
        exp := pow(10, digits);
        return float(round(num * exp)) / exp;
    }
}

// trigonometry

fn cos(x: intOrFloat?) -> float? {
    x = float(x);
    ind := 1;
    xSqr := x * x;
    up := xSqr;
    low := 2;
    bound := cosN * 2;
    res := 1;
    for i := 0; i < bound; i += 2 {
        ind = -ind;
        res += ind * up / low;
        up *= xSqr;
        low *= (i + 3) * (i + 4);
    }
    return res;
}

fn sin(x: intOrFloat?) -> float? {
    return cos(PI / 2 - x);
}

fn tan(x: intOrFloat?) -> float? {
    return sin(x) / cos(x);
}
