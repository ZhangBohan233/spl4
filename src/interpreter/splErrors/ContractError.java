package interpreter.splErrors;

import interpreter.splErrors.NativeError;
import util.LineFile;

public class ContractError extends NativeError {

    public ContractError() {
        super("Contract violation. ");
    }

    public ContractError(LineFile lineFile) {
        super("Contract violation. ", lineFile);
    }

    public ContractError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }
}
