package spl.util;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utilities {

    public static boolean arrayContains(int[] array, int value) {
        for (int a : array) if (a == value) return true;
        return false;
    }

    public static boolean arrayContains(char[] array, char value) {
        for (char a : array) if (a == value) return true;
        return false;
    }

    public static boolean arrayContains(String[] array, String value) {
        for (String a : array) if (a.equals(value)) return true;
        return false;
    }

    public static boolean arrayContains2D(int[][] array, int[] value) {
        for (int[] a : array) if (Arrays.equals(a, value)) return true;
        return false;
    }

    public static void intToBytes(int value, byte[] arr, int index) {
        arr[index] = (byte) (value >> 24);
        arr[index + 1] = (byte) (value >> 16);
        arr[index + 2] = (byte) (value >> 8);
        arr[index + 3] = (byte) value;
    }

    public static byte[] intToBytes(int value) {
        byte[] arr = new byte[4];
        intToBytes(value, arr, 0);
        return arr;
    }

    public static int bytesToInt(byte[] arr, int index) {
        return ((arr[index] & 0xff) << 24) |
                ((arr[index + 1] & 0xff) << 16) |
                ((arr[index + 2] & 0xff) << 8) |
                (arr[index + 3] & 0xff);
    }

    public static int bytesToInt(byte[] arr) {
        return bytesToInt(arr, 0);
    }

    public static void doubleToBytes(double value, byte[] array, int index) {
        long l = Double.doubleToLongBits(value);
        for (int i = 0; i < 8; i++) {
            array[index + i] = (byte) ((l >> 8 * i) & 0xff);
        }
    }

    public static double bytesToDouble(byte[] array, int index) {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v |= ((long) (array[index + i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(v);
    }

    public static byte[] doubleToBytes(double value) {
        byte[] arr = new byte[8];
        doubleToBytes(value, arr, 0);
        return arr;
    }

    public static double bytesToDouble(byte[] array) {
        return bytesToDouble(array, 0);
    }

    public static void longToBytes(long value, byte[] array, int index) {
        for (int i = 0; i < 8; i++) {
            array[index + i] = (byte) ((value >> 8 * i) & 0xff);
        }
    }

    public static long bytesToLong(byte[] array, int index) {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v |= ((long) (array[index + i] & 0xff)) << (8 * i);
        }
        return v;
    }

    public static byte[] longToBytes(long value) {
        byte[] arr = new byte[8];
        longToBytes(value, arr, 0);
        return arr;
    }

    public static long bytesToLong(byte[] arr) {
        return bytesToLong(arr, 0);
    }

    public static byte[] stringToLengthBytes(String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        byte[] res = new byte[b.length + 4];
        intToBytes(b.length, res, 0);
        System.arraycopy(b, 0, res, 4, b.length);
        return res;
    }

//    public static String readLengthString(InputStream is) throws IOException {
//        int length = readInt(is);
//        byte[] buf = new byte[length];
//        if (is.read(buf) != length) throw new IOException("Cannot read string");
//        return new String(buf);
//    }

    public static String readFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            builder.append(line).append('\n');
        }
        br.close();
        return builder.toString();
    }

    public static void writeFile(File file, String text) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(text);
        fw.flush();
        fw.close();
    }

    public static String[] arrayConcatenate(String[]... arrays) {
        int len = 0;
        for (String[] arr : arrays) len += arr.length;
        int index = 0;
        String[] res = new String[len];
        for (String[] arr : arrays) {
            System.arraycopy(arr, 0, res, index, arr.length);
            index += arr.length;
        }
        return res;
    }

    /**
     * Returns the readable {@code String} of <code>size</code>, representing the size of a file.
     * <p>
     * This method shows a number that at most 1,024 and a corresponding suffix
     *
     * @param size the size to be converted
     * @return the readable {@code String}
     */
    public static String sizeToReadable(long size) {
        if (size < Math.pow(2, 10)) return numToReadable2Decimal((int) size) + " B";
        else if (size < Math.pow(2, 20)) return numToReadable2Decimal((double) size / 1024) + " KB";
        else if (size < Math.pow(2, 30)) return numToReadable2Decimal((double) size / 1048576) + " MB";
        else return numToReadable2Decimal((double) size / 1073741824) + "GB";
    }

    public static String numToReadable2Decimal(double num) {
        return num == (int) num ? String.format("%,d", (int) num) : String.format("%,.2f", num);
    }

    @SafeVarargs
    public static Set<String> mergeSets(Set<String>... sets) {
        Set<String> res = new HashSet<>();
        for (Set<String> s : sets) res.addAll(s);

        return res;
    }

    @SafeVarargs
    public static Map<String, Integer> mergeMaps(Map<String, Integer>... maps) {
        return new MapMerger<>(maps).merge();
    }

    public static String typeName(SplElement element, Environment env, LineFilePos lineFilePos) {
        Reference typeFnPtr = (Reference) env.get(Constants.TYPE_FN, lineFilePos);
        SplCallable typeFn = env.getMemory().get(typeFnPtr);
        SplElement res = typeFn.call(EvaluatedArguments.of(element), env, lineFilePos);
        if (res instanceof Reference) {
            SplObject typeObj = env.getMemory().get((Reference) res);
            if (typeObj instanceof SplClass) return ((SplClass) typeObj).getClassName();
            if (typeObj instanceof SplCallable) return ((SplCallable) typeObj).getName();
        }
        return element.getClass().getSimpleName();
    }

    public static String classRefToString(Reference classRef, Environment env) {
        if (classRef.getPtr() == 0) return "null";
        return ((SplClass) env.getMemory().get(classRef)).getClassName();
    }

    public static String classRefToRepr(Reference classRef, Environment env) {
        return "Class<" + classRefToString(classRef, env) + ">";
    }

    /**
     * Removes the exception/error in global environment and then print the error message to standard error stream.
     *
     * @param globalEnvironment the global environment
     * @param lineFile          line and file
     */
    public static void removeErrorAndPrint(GlobalEnvironment globalEnvironment, LineFilePos lineFile) {
        Reference errPtr = globalEnvironment.getExceptionInsPtr();
        globalEnvironment.removeException();

        Instance errIns = globalEnvironment.getMemory().get(errPtr);

        Reference stackTraceFtnPtr = (Reference) errIns.getEnv().get("printStackTrace", lineFile);
        Function stackTraceFtn = globalEnvironment.getMemory().get(stackTraceFtnPtr);
        stackTraceFtn.call(EvaluatedArguments.of(errPtr), globalEnvironment, lineFile);
    }

    public static SplElement unwrap(SplElement ele, Environment env, LineFilePos lineFilePos) {
        if (ele instanceof Reference) {
            SplObject obj = env.getMemory().get((Reference) ele);
            if (obj instanceof Instance) {
                Instance ins = (Instance) obj;
                Reference wrapperClassRef = (Reference) env.get(Constants.WRAPPER, lineFilePos);
                Reference childClassRef = ins.getClazzPtr();
                if (SplClass.isSuperclassOf(wrapperClassRef, childClassRef, env.getMemory())) {
                    return ins.getEnv().get(Constants.WRAPPER_ATTR, lineFilePos);
                }
            }
        }
        return ele;
    }

    public static Reference wrap(SplElement se, Environment env, LineFilePos lineFilePos) {
        if (se instanceof Reference) return (Reference) se;
        else return primitiveToWrapper(se, env, lineFilePos);
    }

    public static Reference primitiveToWrapper(SplElement prim, Environment env, LineFilePos lineFile) {
        String wrapperName = Constants.WRAPPERS.get(prim.type());
        Instance.InstanceAndPtr iap = Instance.createInstanceWithInitCall(
                wrapperName,
                EvaluatedArguments.of(prim),
                env,
                lineFile
        );
        if (iap == null) return Reference.NULL;
        return iap.pointer;
    }

    public static SplElement wrapperToPrimitive(Reference wrapperPtr, Environment env, LineFilePos lineFile) {
        SplObject obj = env.getMemory().get(wrapperPtr);
        if (obj instanceof Instance) {
            Instance wrapperIns = (Instance) obj;
            return wrapperIns.getEnv().get(Constants.WRAPPER_ATTR, lineFile);
        } else {
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Cannot convert '" + obj + "' to primitive.",
                    lineFile);
        }
    }

    public static boolean isInstancePtr(SplElement element, String className, Environment env, LineFilePos lineFile) {
        Reference insFtnPtr = (Reference) env.get(className + "?", lineFile);
        SplCallable insFtn = env.getMemory().get(insFtnPtr);
        Bool res = (Bool) insFtn.call(EvaluatedArguments.of(element), env, lineFile);
        return res.value;
    }

    public static String numberToOrder(int num) {
        String s = String.valueOf(num);
        char last = s.charAt(s.length() - 1);
        return s + switch (last) {
            case '1' -> "st";
            case '2' -> "nd";
            case '3' -> "rd";
            default -> "th";
        };
    }
}
