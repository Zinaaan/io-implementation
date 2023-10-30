package nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author lzn
 * @date 2023/06/18 17:11
 * @description The origin implementation of NIO (Non-blocking IO)
 */
@Slf4j
public class NioV1 {

    /**
     * Store the client connection
     */
    private static final List<SocketChannel> CHANNEL_LIST = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(9001));
            // Set the ServerSocketChannel to non-blocking
            serverSocketChannel.configureBlocking(false);
            log.info("Server started successful...........");

            while (true) {
                // Won't block because current mode is non-blocking
                // The non-blocking IO is implemented by the internal of operation system via accept function of linux kernel.
                SocketChannel socketChannel = serverSocketChannel.accept();
                // If has client connected
                if (socketChannel != null) {
                    log.info("Connection established.............");
                    socketChannel.configureBlocking(false);
                    CHANNEL_LIST.add(socketChannel);
                }

                Iterator<SocketChannel> iterator = CHANNEL_LIST.iterator();
                while (iterator.hasNext()) {
                    SocketChannel sc = iterator.next();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                    int length = sc.read(byteBuffer);
                    // If the client send some messages
                    if (length > 0) {
                        log.info(Thread.currentThread().getName() + " received message：" + new String(byteBuffer.array()));
                    } else if (length == -1) {
                        // If the client has been disconnected, remove the current socket from collections
                        iterator.remove();
                        log.info("Connection has been disconnected............");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
