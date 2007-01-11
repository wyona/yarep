package org.wyona.yarep.core;

public final class PropertyType {
    public static final int STRING = 1;
    public static final int BINARY = 2;
    public static final int LONG = 3;
    public static final int DOUBLE = 4;
    public static final int DATE = 5;
    public static final int BOOLEAN = 6;

    public static final String TYPENAME_STRING = "string";
    public static final String TYPENAME_BINARY = "binary";
    public static final String TYPENAME_LONG = "long";
    public static final String TYPENAME_DOUBLE = "double";
    public static final String TYPENAME_DATE = "date";
    public static final String TYPENAME_BOOLEAN = "boolean";
    
    public static String getTypeName(int type) {
        switch (type) {
            case STRING: return TYPENAME_STRING;
            case BINARY: return TYPENAME_BINARY;
            case LONG: return TYPENAME_LONG;
            case DOUBLE: return TYPENAME_DOUBLE;
            case DATE: return TYPENAME_DATE;
            case BOOLEAN: return TYPENAME_BOOLEAN;
            default: throw new IllegalArgumentException("invalid type: " + type);
        }
    }

    public static int getType(String typeName) {
        if (typeName.equals(TYPENAME_STRING)) return STRING;
        else if (typeName.equals(TYPENAME_BINARY)) return BINARY;
        else if (typeName.equals(TYPENAME_LONG)) return LONG;
        else if (typeName.equals(TYPENAME_DOUBLE)) return DOUBLE;
        else if (typeName.equals(TYPENAME_DATE)) return DATE;
        else if (typeName.equals(TYPENAME_BOOLEAN)) return BOOLEAN;
        else throw new IllegalArgumentException("invalid type: " + typeName);
    }

}