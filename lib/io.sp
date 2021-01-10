const READ = 1;
const READ_B = 2;
const WRITE = 3;
const WRITE_B = 4;


class File {
    const file;
    const mode;

    fn __init__(file: NativeFile?, mode: int?) {
        this.file = file;
        this.mode = mode;
    }

    fn read() {

    }

    fn write() {
    }

    fn close() {
        if not file.close() {
            throw new IOError("Cannot close file " + file);
        }
    }
}

class Text

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