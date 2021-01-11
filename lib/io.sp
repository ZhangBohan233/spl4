class IOError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class InputStream {
    fn close() {
    }
}

class OutputStream {
    fn close() {
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

class FileReader {
    const fis;
    const bufferSize = 4;
    var buffer = new byte[0];
    var bufferPos = 0;

    fn __init__(fileName: String?) {
        fis = new FileInputStream(fileName);
    }

    /*
     * Reads a line from the text file, or null if reaches the end of the file.
     */
    fn readLine() -> String? or null? {
        notFound := true;
        res := [];
        while notFound {
            for ; bufferPos < buffer.length; bufferPos++ {
                res.append(buffer[bufferPos]);
                if buffer[bufferPos] == '\n' {
                    notFound = false;
                    bufferPos++;
                    break;
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
    }

    fn _fill() -> boolean? {
        bufferPos = 0;
        buffer = fis.read(bufferSize);
        return buffer.length > 0;
    }
}
