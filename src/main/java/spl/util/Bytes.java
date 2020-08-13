package spl.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Bytes {

    public static double bytesToDouble(byte[] bytes, int index) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put(bytes, index, 8);
        byteBuffer.flip();
        return byteBuffer.getDouble();
    }

    public static void doubleToBytes(double num, byte[] bytes, int index) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putDouble(num);
        System.arraycopy(byteBuffer.array(), 0, bytes, index, 8);
    }

    public static long bytesToLong(byte[] bytes, int index) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put(bytes, index, 8);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }

    public static void longToBytes(long num, byte[] bytes, int index) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putLong(num);
        System.arraycopy(byteBuffer.array(), 0, bytes, index, 8);
    }

    public static void main(String[] args) {
        byte[] b = new byte[16];
        doubleToBytes(23.45, b, 4);
        System.out.println(Arrays.toString(b));
        System.out.println(bytesToDouble(b, 4));
    }
}
