package com.zero.nts.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据包类型
 *
 * @author Zero.
 * <p> Created on 2025/5/28 16:08 </p>
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    /// 心跳包
    HEART_BEAT((byte) 0),
    /// 数据包
    NORMAL((byte) 1),
    ;
    private final byte value;

    public static MessageType fromType(byte type) {
        for (MessageType item : MessageType.values()) {
            if (item.value == type) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + type);
    }
}
