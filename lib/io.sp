const READ = 1;
const READ_B = 2;
const WRITE = 3;
const WRITE_B = 4;


class File {
    const file;
    const mode;
    const fileLength;

    fn __init__(file: NativeFile?, mode: int?) {
        this.file = file;
        this.fileLength = file.length();
        this.mode = mode;
    }

    fn length() {
        return fileLength;
    }

    fn position() {
        return file.position();
    }

    fn read(size: int? = -1) {
        if size == -1 {
            size = fileLength;
        }
        var res;
        switch mode {
            case READ {
                res = file.readText(size);
            } case READ_B {
                res = file.readBytes(size);
            } default {
                throw new IOError("Unexpected mode to read.");
            }
        }
        return res;
    }

    fn readLine() {

    }

    fn write(data: byte? or String?) {
    }

    fn writeLine(line: String?) {
    }

    fn close() {
        if not file.close() {
            throw new IOError("Cannot close file " + file);
        }
    }
}

class IOError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

/*
 * Modes: r, w, rb, wb
 */
fn open(file: String?, mode: String?) -> File? or null? {
    var m = switch mode {
        case "r" -> READ;
        case "w" -> WRITE;
        case "rb" -> READ_B;
        case "wb" -> WRITE_B;
        default -> {
            throw new IOError("Unknown mode " + mode);
        };
    }

    nf := Invokes.openFile(file, m);
    if nf is null {
        throw new IOError("Failed to open " + file);
    }
    return new File(nf, m);
}