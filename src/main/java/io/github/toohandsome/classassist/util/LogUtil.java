package io.github.toohandsome.classassist.util;

import io.github.toohandsome.classassist.config.ClassAssistConfig;

public class LogUtil {

    public static void info(String msg) {
        if (ClassAssistConfig.log) {
            System.out.println(msg);
        }
    }

    public static void error(String msg) {
        if (ClassAssistConfig.log) {
            System.err.println(msg);
        }
    }
}
