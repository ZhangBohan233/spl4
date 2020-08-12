macro add {
    syntax $a $b {
        $a + $b
    }
}

macro match {
    syntax $a {
        lambda (*args) -> $a(*args);
    }

    syntax $a or $b... {
        lambda (*args) -> true if $a(*args) else match($b...);
    }
}

fn main() {
    b := add(3 or 4);
    print(b);
    //f := match(int?);
}
