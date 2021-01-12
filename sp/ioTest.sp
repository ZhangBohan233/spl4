import namespace io

fn main() {
    ism := new FileReader("Untitled.sp");

    print(ism.read());

    ism.close();

    osm := new FileWriter("no.sp");
    osm.write("fuck");
    osm.writeLine("fuck that");
    osm.writeLine("shit!!");
    osm.flush();
    osm.close();
}
