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

    return new File(Invokes.openFile(file, m), m);
}