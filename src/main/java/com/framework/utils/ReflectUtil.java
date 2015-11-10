package com.framework.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReflectUtil {
    public static Pair<Class<?>, Field> getAnyField(Class<?> clazz, String fieldName) {
        try {
            return new Pair<Class<?>, Field>(clazz, clazz.getDeclaredField(fieldName));
        } catch (SecurityException e) {
            throw new RuntimeException("How the heck?", e);
        } catch (NoSuchFieldException e) {
            // Do I really want this? No I don't but what can I do? It only
            // throws the NoSuchFieldException.
            Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                return getAnyField(parent, fieldName);
            }
            return null;
        }
    }

    public static Method findMethod(Class<?> clazz, String methodName) {
        do {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return null;
    }

    // Checks against posted search classes if cmd is async
    public static boolean isCmdClassAsync(Class<?> cmdClass, Class<?>[] searchClasses) {
        boolean isAsync = false;
        Class<?> superClass = cmdClass;

        while (superClass != null && superClass != Object.class) {
            String superName = superClass.getName();
            for (Class<?> baseClass : searchClasses) {
                if (superName.equals(baseClass.getName())) {
                    isAsync = true;
                    break;
                }
            }
            if (isAsync)
                break;
            superClass = superClass.getSuperclass();
        }
        return isAsync;
    }

    // Returns all fields until a base class for a cmd class
    public static List<Field> getAllFieldsForClass(Class<?> cmdClass, Class<?> baseClass) {
        List<Field> fields = new ArrayList<Field>();
        Collections.addAll(fields, cmdClass.getDeclaredFields());
        Class<?> superClass = cmdClass.getSuperclass();
        while (baseClass.isAssignableFrom(superClass)) {
            Field[] superClassFields = superClass.getDeclaredFields();
            if (superClassFields != null)
                Collections.addAll(fields, superClassFields);
            superClass = superClass.getSuperclass();
        }
        return fields;
    }

    // Returns all unique fields except excludeClasses for a cmd class
    public static Set<Field> getAllFieldsForClass(Class<?> cmdClass, Class<?>[] excludeClasses) {
        Set<Field> fields = new HashSet<Field>();
        Collections.addAll(fields, cmdClass.getDeclaredFields());
        Class<?> superClass = cmdClass.getSuperclass();

        while (superClass != null && superClass != Object.class) {
            String superName = superClass.getName();
            boolean isNameEqualToSuperName = false;
            for (Class<?> baseClass : excludeClasses)
                if (superName.equals(baseClass.getName()))
                    isNameEqualToSuperName = true;

            if (!isNameEqualToSuperName) {
                Field[] superClassFields = superClass.getDeclaredFields();
                if (superClassFields != null)
                    Collections.addAll(fields, superClassFields);
            }
            superClass = superClass.getSuperclass();
        }
        return fields;
    }

}
