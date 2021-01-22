class IOError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class FileInputStream(InputStream) {
    const file;

    fn __init__(fileName: String?) {
        file = Invokes.openInputFile(fileName);
        if file == null {
            throw new IOError("Cannot open file " + fileName);
        }
    }

    fn readOne() -> int? {
        b := file.read(1);
        if b is null {
            throw new IOError("Cannot read file " + fileName);
        }
        if b.length == 0 {
            return -1;
        } else {
            return int(b[0]);
        }
    }

    fn read(length: int?) -> array?(byte) {
        b := file.read(length);
        if b is null {
            throw new IOError("Cannot read file " + fileName);
        }
        return b;
    }

    fn close() {
        if not file.close() {
            throw new IOError("Cannot close file " + fileName);
        }
    }
}

class FileOutputStream(OutputStream) {
    const file;

    fn __init__(fileName: String?) {
        file = Invokes.openOutputFile(fileName);
        if file is null {
            throw new IOError("Cannot open file " + fileName);
        }
    }

    fn writeOne(b: byte?) {
        buffer := new byte[1];
        buffer[0] = b;
        if not file.write(buffer) {
            throw new IOError("Cannot write file " + fileName);
        }
    }

    fn write(data: array?(byte)) {
        if not file.write(data) {
            throw new IOError("Cannot write file " + fileName);
        }
    }

    fn flush() {
        if not file.flush() {
            throw new IOError("Cannot close file " + fileName);
        }
    }

    fn close() {
        if not file.close() {
            throw new IOError("Cannot close file " + fileName);
        }
    }
}

class FileReader(StreamReader) {
    fn __init__(fileName: String?) {
        super.__init__(new FileInputStream(fileName));
    }
}

class FileWriter {
    const fos;

    fn __init__(fileName: String?) {
        this.fos = new FileOutputStream(fileName);
    }

    fn write(text: String?) {
        arr := Invokes.stringToBytes(text);
        fos.write(arr);
    }

    fn writeLine(line: String?) {
        write(line);
        write("\n");
    }

    fn flush() {
        fos.flush();
    }

    fn close() {
        fos.close();
    }
}
