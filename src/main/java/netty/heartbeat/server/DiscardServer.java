package netty.heartbeat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import netty.heartbeat.handler.HeartbeatServerHandler;

import java.nio.charset.Charset;

/**
 * @author lzn
 * @date 2023/06/18 22:24
 * @description Netty server
 */
public class DiscardServer {

    private final int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() {
        // Default value is twice of the number of core cpu
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // parameter for new IdleStateHandler(read timeout, write timeout, all timeout)
                            // Throw exception If server have no read/write/all performed for the specified period of time
                            socketChannel.pipeline().addLast(new IdleStateHandler(2, 2, 5));
                            socketChannel.pipeline().addLast(new HeartbeatServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            System.out.println("tcp start success");
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to graceful
            // shut down your server
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}
