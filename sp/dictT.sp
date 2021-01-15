
fn main() {
    dict := new HashDict();
    dict.put(1, 3);
    dict.put(2, 4);
    dict[9] = 5;
    dict[9] = 6;
    dict[7] = 3;
    dict[12] = 2;
    dict[4] = -2;
    dict[18] = 1.2;
    dict.remove(1);
    for key in dict {
        print(key, line=false);
        print(": ", line=false);
        print(dict[key]);
    }
    return dict.size();
}