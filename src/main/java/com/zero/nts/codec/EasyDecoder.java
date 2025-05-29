package com.zero.nts.codec;

import com.zero.nts.message.EasyMessage;
import com.zero.nts.message.MessageType;
import com.zero.nts.message.MessageVersion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 消息解码器
 *
 * @author Zero.
 * <p> Created on 2025/5/27 11:52 </p>
 */
@Slf4j
public class EasyDecoder extends ByteToMessageDecoder {
    private final int MESSAGE_MAX_SIZE = 1024 * 1024 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < EasyMessage.MESSAGE_FIXED_SIZE) {
            return;
        }
        // 标记读取位置,方便后续回滚
        in.markReaderIndex();

        // 读取报文固定内容,
        // TODO 校验合法性：魔术值、版本、类型、以及最大长度等
        char magic = in.readChar();
        byte version = in.readByte();
        byte type = in.readByte();
        long id = in.readLong();
        long timestamp = in.readLong();
        int length = in.readInt();

        // 校验数据包长度
        if (length < 0 || length > MESSAGE_MAX_SIZE) {
            log.error("[EasyDecoder] Invalid message length {}", length);
            ctx.close();
            return;
        }

        // 如果可读数据不足数据包长度，则需要重置并等待更多数据的到来（可能发生了网络活动或粘包）
        if (in.readableBytes() < length) {
            log.debug("[EasyDecoder] Data body incomplete, reset buf, channel: {}", ctx.channel().remoteAddress());
            in.resetReaderIndex();
            return;
        }

        // 读取数据体
        byte[] payload = null;
        if (length > 0) {
            payload = new byte[length];
            in.readBytes(payload);
        }
        EasyMessage message = new EasyMessage(magic, MessageVersion.fromVersion(version), MessageType.fromType(type), id, timestamp, length, payload);
        out.add(message);
    }
}
