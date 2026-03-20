package com.emoseman.hidpi.util;

import com.intellij.ide.ui.UISettings;

import java.lang.reflect.Method;

public final class ReflectionUiSettingsAccessor {
    private ReflectionUiSettingsAccessor() {
    }

    public static String getUiFontFamily(UISettings uiSettings) {
        return invokeString(uiSettings, "getFontFace");
    }

    public static Integer getUiFontSize(UISettings uiSettings) {
        return invokeInt(uiSettings, "getFontSize");
    }

    public static Integer getPresentationModeFontSize(UISettings uiSettings) {
        return invokeInt(uiSettings, "getPresentationModeFontSize");
    }

    public static boolean setUiFontFamily(UISettings uiSettings, String family) {
        return invokeSetter(uiSettings, "setFontFace", String.class, family);
    }

    public static boolean setUiFontSize(UISettings uiSettings, int size) {
        return invokeSetter(uiSettings, "setFontSize", int.class, size);
    }

    public static boolean setPresentationModeFontSize(UISettings uiSettings, int size) {
        return invokeSetter(uiSettings, "setPresentationModeFontSize", int.class, size);
    }

    private static String invokeString(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object result = method.invoke(target);
            return result instanceof String ? (String) result : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static Integer invokeInt(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object result = method.invoke(target);
            return result instanceof Integer ? (Integer) result : -1;
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static boolean invokeSetter(Object target, String methodName, Class<?> type, Object value) {
        try {
            Method method = target.getClass().getMethod(methodName, type);
            method.invoke(target, value);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
