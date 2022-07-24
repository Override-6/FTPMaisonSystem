/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.internal.manipulation.creation;

import java.lang.reflect.Field;

public class ObjectCreator {

    public static native Object allocate(Class<?> clazz);

    public static void pasteAllFields(Object target, Field[] fields, Object[] fieldValues) {
        if (fields.length != fieldValues.length) {
            throw new IllegalArgumentException("Field names, field types and field values arrays must have the same length.");
        }
        String[] fieldReturnTypes = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldReturnTypes[i] = fields[i].getType().getName();
        }
        pasteAllFields0(target, fields, fieldReturnTypes, fieldValues);
    }

    public static Object[] getAllFields(Object target, Field[] fields) {
        String[] fieldReturnTypes = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldReturnTypes[i] = fields[i].getType().getName();
        }
        return getAllFields(target, fields, fieldReturnTypes);
    }

    private static native void pasteAllFields0(Object target, Field[] fields, String[] fieldReturnTypes, Object[] fieldValues);

    private static native Object[] getAllFields(Object target, Field[] fields, String[] fieldReturnTypes);

}