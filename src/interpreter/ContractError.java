package interpreter;

import util.LineFile;

public class ContractError extends SplException {

    public ContractError() {
        super("Contract violation. ");
    }

    public ContractError(LineFile lineFile) {
        super("Contract violation. ", lineFile);
    }
}
