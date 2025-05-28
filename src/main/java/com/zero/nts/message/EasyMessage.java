package com.zero.nts.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Zero.
 * <p> Created on 2025/5/26 15:29 </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EasyMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** 协议标识 */
    private char magic = '@';
    /** 协议版本: V1 */
    private byte version;
    /** 消息类型 */
    private byte type;
    /** 数据体长度 */
    private int length;
    /** 数据体 */
    private byte[] data;

    /**
     * 序列化为字节流
     * @return {@link ByteBuf}
     */
    public ByteBuf toByteBuf() {
        ByteBuf buf = Unpooled.buffer(2 + 1 + 1 + 4 + Math.max(length, data.length));
        buf.writeShort(magic)
                .writeByte(version)
                .writeByte(type)
                .writeInt(length)
                .writeBytes(data);
        return buf;
    }

    /**
     * 反序列化为消息体
     * @param buf 字节流
     * @return {@link EasyMessage}
     */
    public static EasyMessage fromByteBuf(ByteBuf buf) {
        EasyMessage message = new EasyMessage();
        message.magic = buf.readChar();
        message.version = buf.readByte();
        message.type = buf.readByte();
        message.length = buf.readInt();
        if (message.length > 0) {
            message.data = new byte[message.length];
            buf.readBytes(message.data);
        }
        return message;
    }

    public static void main(String[] args) {
        EasyMessage message = new EasyMessage();
        message.setMagic('@');
        message.setVersion((byte) 1);
        message.setType((byte) 1);

        byte[] payload = "Hello, world!".getBytes();
        message.setLength(payload.length);
        message.setData(payload);

        ByteBuf byteBuf = message.toByteBuf();
        byte[] array = byteBuf.array();
        System.out.println(Arrays.toString(array));
        System.out.println(array.length);
        // [0, 64, 1, 1, 0, 0, 0, 13, 72, 101, 108, 108, 111, 44, 32, 119, 111, 114, 108, 100, 33]

        EasyMessage body = EasyMessage.fromByteBuf(byteBuf);
        System.out.println(body);
    }
}
