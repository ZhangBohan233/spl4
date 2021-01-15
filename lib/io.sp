class IOError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class InputStream {
    fn close() {
    }

    /*
     * Reads one byte from the stream,
     */
    fn readOne() -> int? {
        throw new NotImplementedError();
    }
}

class OutputStream {
    fn close() {
    }

    /*
     * Writes one byte to the stream.
     */
    fn writeOne(b: byte?) {
        throw new NotImplementedError();
    }

    /*
     * Writes all buffered data to the actual stream.
     */
    fn flush() {
        throw new NotImplementedError();
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

class FileReader {
    const fis;
    const bufferSize = 64;
    var buffer = new byte[0];
    var bufferPos = 0;

    fn __init__(fileName: String?) {
        fis = new FileInputStream(fileName);
    }

    /*
     * Reads a line from the text file, or null if reaches the end of the file.
     */
    fn readLine(omitEol: boolean? = false) -> String? or null? {
        notFound := true;
        res := [];
        while notFound {
            for ; bufferPos < buffer.length; bufferPos++ {
                b := buffer[bufferPos];
                if buffer[bufferPos] == '\n' {
                    if not omitEol {
                        res.append(b);
                    }
                    notFound = false;
                    bufferPos++;
                    break;
                } else {
                    res.append(b);
                }
            }
            if notFound {
                if not _fill() {
                    break;
                }
            }
        }
        arr := res.toArray(byte);
        return Invokes.bytesToString(arr) if arr.length > 0 else null;
    }

    fn close() {
        fis.close();
    }

    /*
     * Reads the whole file as one string.
     */
    fn read() -> String? {
        lst := [];
        var s;
        while (s = readLine()) is not null {
            lst.append(s);
        }
        return strJoin("", lst);
    }

    fn _fill() -> boolean? {
        bufferPos = 0;
        buffer = fis.read(bufferSize);
        return buffer.length > 0;
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
