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
import java.util.*;

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
     * @param size   the size to be converted
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
        Map<String, Integer> res = new HashMap<>();
        for (Map<String, Integer> m : maps) res.putAll(m);
        return res;
    }

    public static String typeName(SplElement element) {
        return element.getClass().toString();
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
    public static void removeErrorAndPrint(GlobalEnvironment globalEnvironment, LineFile lineFile) {
        Reference errPtr = globalEnvironment.getExceptionInsPtr();
        globalEnvironment.removeException();

        Instance errIns = (Instance) globalEnvironment.getMemory().get(errPtr);

        Reference stackTraceFtnPtr = (Reference) errIns.getEnv().get("printStackTrace", lineFile);
        Function stackTraceFtn = (Function) globalEnvironment.getMemory().get(stackTraceFtnPtr);
        stackTraceFtn.call(EvaluatedArguments.of(errPtr), globalEnvironment, lineFile);
    }

    public static Reference primitiveToWrapper(SplElement prim, Environment env, LineFile lineFile) {
        String wrapperName = Constants.WRAPPERS.get(prim.type());
        return Instance.createInstanceWithInitCall(
                wrapperName,
                EvaluatedArguments.of(prim),
                env,
                lineFile
        ).pointer;
    }

    public static SplElement wrapperToPrimitive(Reference wrapperPtr, Environment env, LineFile lineFile) {
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

    public static boolean isInstancePtr(SplElement element, String className, Environment env, LineFile lineFile) {
        Reference insFtnPtr = (Reference) env.get(className + "?", lineFile);
        SplCallable insFtn = (SplCallable) env.getMemory().get(insFtnPtr);
        Bool res = (Bool) insFtn.call(EvaluatedArguments.of(element), env, lineFile);
        return res.value;
    }
}
