package com.kuopan.Entity.enums;

import lombok.Getter;

@Getter
public enum SHAEnum {
    SHA_1("SHA-1"),
    SHA_224("SHA-224"),
    SHA_256("SHA-256"),
    SHA_384("SHA-384"),
    SHA_512("SHA-512");


    private String SHAType;

    SHAEnum(String SHAType) {
        this.SHAType = SHAType;
    }

    public static boolean hasType(String SHAType) {
        if (SHAType == null) {
            return false;
        }
        for (int i = 0; i < SHAEnum.values().length; i++) {
            if (SHAEnum.values()[i].getSHAType().equals(SHAType)) {
                return true;
            }
        }
        return false;
    }
}
