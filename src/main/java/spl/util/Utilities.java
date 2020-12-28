package spl.util;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.NativeTypeError;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplCallable;
import spl.interpreter.splObjects.SplObject;

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

    public static Pointer primitiveToWrapper(SplElement prim, Environment env, LineFile lineFile) {
        String wrapperName = Constants.WRAPPERS.get(prim.type());
        return Instance.createInstanceWithInitCall(
                wrapperName,
                EvaluatedArguments.of(prim),
                env,
                lineFile
        ).pointer;
    }

    public static SplElement wrapperToPrimitive(Pointer wrapperPtr, Environment env, LineFile lineFile) {
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
        Pointer insFtnPtr = (Pointer) env.get(className + "?", lineFile);
        SplCallable insFtn = (SplCallable) env.getMemory().get(insFtnPtr);
        Bool res = (Bool) insFtn.call(EvaluatedArguments.of(element), env, lineFile);
        return res.value;
    }
}
