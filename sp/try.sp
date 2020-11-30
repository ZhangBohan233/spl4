fn main() {

    try {
        throw new Exception();
    } catch Exception? as e {
        print("Caught");
    }
}