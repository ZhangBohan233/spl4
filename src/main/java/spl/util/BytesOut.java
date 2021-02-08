package spl.util;

import spl.ast.Node;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class BytesOut extends BufferedOutputStream {

    public BytesOut(OutputStream out) {
        super(out);
    }

    public void writeInt(int value) throws IOException {
        write(Utilities.intToBytes(value));
    }

    public void writeLong(long value) throws IOException {
        write(Utilities.longToBytes(value));
    }

    public void writeDouble(double value) throws IOException {
        write(Utilities.doubleToBytes(value));
    }

    public void writeBoolean(boolean value) throws IOException {
        write(value ? 1 : 0);
    }

    public void writeChar(char c) throws IOException {
        String s = String.valueOf(c);
        writeString(s);
    }

    public void writeString(String s) throws IOException {
        write(Utilities.stringToLengthBytes(s));
    }

    public void writeList(List<? extends Node> list) throws IOException {
        writeInt(list.size());
        for (Node node : list) {
            node.save(this);
        }
    }

    public <T> void writeOptional(T nodeListNull) throws IOException {
        writeBoolean(nodeListNull != null);
        if (nodeListNull != null) {
            if (nodeListNull instanceof Node) {
                ((Node) nodeListNull).save(this);
            } else if (nodeListNull instanceof List) {
                writeList((List<? extends Node>) nodeListNull);
            } else {
                throw new IOException("Not supported type.");
            }
        }
    }
}
