package com.zero.nts.codec;

import com.zero.nts.message.EasyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 *
 * @author Zero.
 * <p> Created on 2025/5/27 11:51 </p>
 */
public class EasyEncoder extends MessageToByteEncoder<EasyMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, EasyMessage msg, ByteBuf out) throws Exception {
        ByteBuf buf = msg.toByteBuf();
        out.writeBytes(buf);
        buf.release();
    }
}
