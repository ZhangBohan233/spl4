package spl.util;

import spl.ast.Node;
import spl.ast.StringLiteral;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytesIn extends BufferedInputStream {

    public final Map<String, StringLiteral> literalMap = new HashMap<>();

    public BytesIn(InputStream in) {
        super(in);
    }

    public byte readByte() throws IOException {
        int r = read();
        if (r < 0) throw new IOException("Cannot read a byte");
        return (byte) r;
    }

    public int readInt() throws IOException {
        byte[] buf4 = new byte[4];
        if (read(buf4) != 4) throw new IOException("Cannot read an integer");
        return Utilities.bytesToInt(buf4);
    }

    public long readLong() throws IOException {
        byte[] buf8 = new byte[8];
        if (read(buf8) != 8) throw new IOException("Cannot read a long");
        return Utilities.bytesToLong(buf8);
    }

    public double readDouble() throws IOException {
        byte[] buf8 = new byte[8];
        if (read(buf8) != 8) throw new IOException("Cannot read a long");
        return Utilities.bytesToDouble(buf8);
    }

    public boolean readBoolean() throws IOException {
        return read() == 1;
    }

    public char readChar() throws IOException {
        String s = readString();
        return s.charAt(0);
    }

    public String readString() throws IOException {
        int length = readInt();
        byte[] buf = new byte[length];
        if (read(buf) != length) throw new IOException("Cannot read string");
        return new String(buf);
    }

    public <T extends Node> List<T> readList() throws Exception {
        int size = readInt();
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(Reconstructor.reconstruct(this));
        }
        return list;
    }

    public <T extends Node> T readOptional() throws Exception {
        boolean b = readBoolean();
        if (b) return Reconstructor.reconstruct(this);
        else return null;
    }
}
