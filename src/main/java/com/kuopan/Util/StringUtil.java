package com.kuopan.Util;

import org.apache.commons.lang3.RandomStringUtils;

public class StringUtil {

    /**
     * Generate Random Number
     *
     * @param count
     * @return
     * */
    public static final String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    public static boolean isEmpty(String s) {
        if (s == null || "".equals(s) || "null".equals(s) || "\u0000".equals(s)) {
            return true;
        } else if ("".equals(s.trim())) {
            return true;
        }
        return false;
    }

}
