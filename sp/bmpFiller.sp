
fn fill(arr2d) {
    for i := 0; i < arr2d.length; i++ {
        row := arr2d[i];
        print("[", false);
        for j := 0; j < row.length; j++ {
            if row[j] {
                print("*", false);
            } else {
                print(" ", false);
            }
        }
        print("]");
    }
}