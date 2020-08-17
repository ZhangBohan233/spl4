package spl.util;

import spl.interpreter.primitives.SplElement;

import java.util.Map;

public class Constants {

    public static final String MAIN_FN = "main";
    public static final String INVOKES = "Invokes";
    public static final String OBJECT_CLASS = "Object";
    public static final String STRING_CLASS = "String";
    public static final String EXCEPTION_CLASS = "Exception";
    public static final String ITERATOR_CLASS = "Iterator";
    public static final String ITERABLE_CLASS = "Iterable";
    public static final String ARRAY_ITERATOR_CLASS = "ArrayIterator";
    public static final String LIST_CLASS = "List";
    public static final String NAIVE_DICT = "NaiveDict";
    public static final String THIS = "this";
    public static final String SUPER = "super";
    public static final String CLASS_NAME = "__name__";
    public static final String CONSTRUCTOR = "__init__";
    public static final String TO_STRING_FN = "__str__";
    public static final String TO_REPR_FN = "__repr__";
    public static final String ITER_FN = "__iter__";
    public static final String NEXT_FN = "__next__";
    public static final String HAS_NEXT_FN = "__hasNext__";
    public static final String GET_ITEM_FN = "__getItem__";
    public static final String SET_ITEM_FN = "__setItem__";
    public static final String STRING_CHARS = "__chars__";
    public static final String CLASS_MRO = "__mro__";
    public static final String WRAPPER_ATTR = "value";
    public static final String GET_CLASS = "__class__";
    public static final String ARRAY_LENGTH = "length";

    public static final Map<Integer, String> WRAPPERS = Map.of(
            SplElement.INT, "Integer",
            SplElement.FLOAT, "Float",
            SplElement.BOOLEAN, "Boolean",
            SplElement.CHAR, "Character"
    );

    public static final Map<String, Integer> WRAPPERS_INV = Map.of(
            "Integer", SplElement.INT,
            "Float", SplElement.FLOAT,
            "Boolean", SplElement.BOOLEAN,
            "Character", SplElement.CHAR
    );
}
