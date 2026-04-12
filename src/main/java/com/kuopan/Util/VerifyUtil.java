package com.kuopan.Util;

import com.kuopan.Entity.enums.VerifyRegexEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyUtil {

    public static boolean verify(String regex, String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(value);
        return m.matches();
    }

    public static boolean verify(VerifyRegexEnum regex, String value) {
        return verify(regex.getRegex(), value);
    }

}
