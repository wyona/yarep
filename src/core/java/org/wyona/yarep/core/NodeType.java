package org.wyona.yarep.core;

public final class NodeType {
    public static final int RESOURCE = 1;
    public static final int COLLECTION = 2;

    public static final String TYPENAME_RESOURCE = "resource";
    public static final String TYPENAME_COLLECTION = "collection";
    
    public static String getTypeName(int type) {
        switch (type) {
            case RESOURCE: return TYPENAME_RESOURCE;
            case COLLECTION: return TYPENAME_COLLECTION;
            default: throw new IllegalArgumentException("invalid type: " + type);
        }
    }

    public static int getType(String typeName) {
        if (typeName.equals(TYPENAME_RESOURCE)) return RESOURCE;
        else if (typeName.equals(TYPENAME_COLLECTION)) return COLLECTION;
        else throw new IllegalArgumentException("invalid type: " + typeName);
    }

}