package netty.chat.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lzn
 * @date 2023/06/18 22:42
 * @description Netty server handler
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    static Set<Channel> channelList = new HashSet<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Informed other clients that I'm online
        channelList.forEach(e -> {
            e.writeAndFlush("[Client]: " + ctx.channel().remoteAddress() + " online");
        });

        // Got current login user and put in to channel list
        channelList.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Distribute to all clients in the chatting room

        String message = msg.toString();

        channelList.forEach(e -> {
            if (e == ctx.channel()) {
                e.writeAndFlush("[Mine]: " + message);
            } else {
                e.writeAndFlush("[Client]: " + ctx.channel().remoteAddress() + ": " + message);
            }
        });
    }

    /**
     * Invoked when channel is inactive
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Informed other clients that I'm offline
        channelList.remove(ctx.channel());
        channelList.forEach(e -> e.writeAndFlush("[Client]: " + ctx.channel().remoteAddress() + " is offline"));
    }
}
