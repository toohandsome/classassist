package com.yxd.classassist.util;

import org.springframework.util.StringUtils;

public class StringUtil {
    public static boolean isNotEmpty(String str) {
        return !"null".equals(str) && StringUtils.hasText(str);
    }
}
