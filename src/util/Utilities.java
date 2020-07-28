package util;

import interpreter.EvaluatedArguments;
import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.Int;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Instance;
import interpreter.splObjects.SplCallable;
import interpreter.splObjects.SplClass;

import java.util.*;
import java.util.function.BiFunction;

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

//    @SafeVarargs
//    public static Map<String, TypeValue> mergeMapsTV(Map<String, TypeValue>... maps) {
//        Map<String, TypeValue> res = new HashMap<>();
//        for (Map<String, TypeValue> m : maps) res.putAll(m);
//        return res;
//    }

    public static String typeName(SplElement element) {
        return element.getClass().toString();
    }

    public static Pointer primitiveToWrapper(SplElement prim, Environment env, LineFile lineFile) {
        String wrapperName = Constants.WRAPPERS.get(prim.type());
        Pointer clazzPtr = (Pointer) env.get(wrapperName, lineFile);
        Instance.InstanceAndPtr wrapperIns = Instance.createInstanceAndAllocate(clazzPtr, env, lineFile);
        Instance.callInit(wrapperIns.instance, EvaluatedArguments.of(prim), env, lineFile);
        return wrapperIns.pointer;
    }

    public static SplElement wrapperToPrimitive(Pointer wrapperPtr, Environment env, LineFile lineFile) {
        Instance wrapperIns = (Instance) env.getMemory().get(wrapperPtr);
        return wrapperIns.getEnv().get(Constants.WRAPPER_ATTR, lineFile);
    }

    public static boolean isInstancePtr(SplElement element, String className, Environment env, LineFile lineFile) {
        Pointer insFtnPtr = (Pointer) env.get(className + "?", lineFile);
        SplCallable insFtn = (SplCallable) env.getMemory().get(insFtnPtr);
        Bool res = (Bool) insFtn.call(EvaluatedArguments.of(element), env, lineFile);
        return res.value;
    }
}
