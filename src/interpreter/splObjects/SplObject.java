package interpreter.splObjects;

public class SplObject {

    private int gcCount;

    public void incrementGcCount() {
        gcCount++;
    }

    public void clearGcCount() {
        gcCount = 0;
    }

    public boolean isGcMarked() {
        return gcCount > 0;
    }

    public int getGcCount() {
        return gcCount;
    }
}
