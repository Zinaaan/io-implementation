package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author lzn
 * @date 2023/06/18 17:11
 * @description The optimized implementation of NIO (Non-blocking IO) via Multiplexer(Selector)
 * <p>
 * What's new:
 * Old: Add each socket events to array, and continuously loop to check and process it even if this socket didn't send any message
 * <p>
 * New: Initialize a Selector to receive the socket events. The selector will start processing only when there are some socket registered.
 */
public class NioV2 {

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(9001));
            serverSocket.configureBlocking(false);

            // Open the Multiplexer(selector) to handle channel -> epoll
            Selector selector = Selector.open();
            // Register serverSocketChannel to the selector
            SelectionKey selectionKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("The server started........");

            while (true) {
                // Blocked, waiting for the ongoing socket coming
                selector.select();

                // Got all instances which registered in the selector
                Set<SelectionKey> selectionKeys = selector.keys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // If current events are OP_ACCEPT, start registering the events and establish the connection
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        serverSocketChannel.configureBlocking(false);
                        // Only registered the read-only events, we can register write events if server need to write message to the client
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("The Client has been connected............");
                    } else if (key.isReadable()) {
                        // If current events are OP_READ, read and output the message
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                        int length = socketChannel.read(byteBuffer);
                        // Output message if the client send any message to the server
                        if (length > 0) {
                            System.out.println(Thread.currentThread().getName() + " received message：" + new String(byteBuffer.array()));
                        } else if (length != -1) {
                            // Close socket if the client was already disconnected
                            System.out.println("The client has been disconnected");
                            socketChannel.close();
                        }
                    }

                    // Remove the current event to avoid repeat processing.
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
