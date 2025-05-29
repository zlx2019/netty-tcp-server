package com.zero.nts.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 协议版本
 *
 * @author Zero.
 * <p> Created on 2025/5/28 15:58 </p>
 */
@Getter
@AllArgsConstructor
public enum MessageVersion {
    V1((byte) 1)
    ;
    private final byte value;

    public static MessageVersion fromVersion(byte version) {
        for (MessageVersion item : MessageVersion.values()) {
            if (item.value == version) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid version: " + version);
    }
}
