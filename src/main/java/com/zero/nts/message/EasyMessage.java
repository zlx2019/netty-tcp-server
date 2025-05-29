package com.zero.nts.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Zero.
 * <p> Created on 2025/5/26 15:29 </p>
 */
@Data
@AllArgsConstructor
public class EasyMessage {
    /** 协议标识 */
    private char magic = '@';
    /** 协议版本: V1 */
    private MessageVersion version;
    /** 消息类型 */
    private MessageType type;
    /** 消息ID */
    private long id;
    /** 时间戳 */
    private long timestamp;
    /** 数据体长度 */
    private int length;
    /** 数据体 */
    private byte[] data;

    public EasyMessage() {
    }

    public EasyMessage(MessageVersion version, MessageType type, long id, long timestamp, byte[] data) {
        this.version = version;
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
        this.length = data.length;
        this.data = data;
    }

    public static final int MESSAGE_FIXED_SIZE = 2 + 1 + 1 + 8 + 8 + 4;

    /**
     * 序列化为字节流
     * @return {@link ByteBuf}
     */
    public ByteBuf toByteBuf() {
        ByteBuf buf = Unpooled.buffer(MESSAGE_FIXED_SIZE + Math.max(length, data.length));
        buf.writeShort(magic)
                .writeByte(version.getValue())
                .writeByte(type.getValue())
                .writeLong(id)
                .writeLong(timestamp)
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
        message.version = MessageVersion.fromVersion(buf.readByte());
        message.type = MessageType.fromType(buf.readByte());
        CharSequence sequence = buf.readCharSequence(32, StandardCharsets.UTF_8);
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
        message.setVersion(MessageVersion.V1);
        message.setType(MessageType.NORMAL);

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
