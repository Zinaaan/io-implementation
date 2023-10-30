package netty.mychat.mydecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author lzn
 * @date 2023/06/20 14:19
 * @description Customized message decoder
 * <p>
 * data format: length + data, eg: 41234
 */
@Slf4j
public class CustomDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        // Check whether the data is valid (the length of a int variable is 4)
        if (byteBuf.readableBytes() < 4) {
            return;
        }

        int length = byteBuf.readInt();
        // If the remaining data size less than

        // Typically, if the data is 41234, the <length> is 4, data is 1234, so the total expect length will be 20
        // However, if the data is 4123, the readBytes() method will throw exception as we supposed to read 16 bytes but obviously there are only 12 bytes to read
        // To resolve this issues we using byteBuf.readableBytes() to check whether the remaining data size is sufficient.
        if (byteBuf.readableBytes() < length) {
            // If the remaining data is insufficient, reset the read index to the previous position (last execution of byteBuf.markReaderIndex()) and return
            // Why? Because this code {int length = byteBuf.readInt()} has already read 4 bytes, so we have to reset the read index to the previous position
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        // Mark the read index in order to the next reading process
        byteBuf.markReaderIndex();
        log.info(new String(bytes));
    }
}
