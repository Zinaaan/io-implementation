package nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
@Slf4j
public class NioV2 {

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(9001));
            serverSocket.configureBlocking(false);

            // Open the Multiplexer(selector) to handle channel -> epoll
            Selector selector = Selector.open();
            // Register serverSocketChannel to the selector
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            log.info("The server started........");
            ByteBuffer readBuffer = ByteBuffer.allocate(128);
            // Map to store client-specific received data
            Map<SocketChannel, StringBuilder> clientDataMap = new HashMap<>();
            while (true) {
                // Blocked, waiting for the ongoing socket coming
                selector.select();

                // Got all instances which registered in the selector
                Set<SelectionKey> selectionKeys = selector.keys();

                for (SelectionKey key : selectionKeys) {
                    // If current events are OP_ACCEPT, start registering the events and establish the connection
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        serverSocketChannel.configureBlocking(false);
                        // Only registered the read-only events, we can register write events if server need to write message to the client
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        log.info("The Client has been connected............");
                        key.cancel();
                    } else if (key.isValid() && key.isReadable()) {
                        // If current events are OP_READ, read and output the message
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        StringBuilder receivedData = clientDataMap.computeIfAbsent(socketChannel, k -> new StringBuilder());
                        int bytesRead;
                        while ((bytesRead = socketChannel.read(readBuffer)) > 0) {
                            readBuffer.flip();
                            byte[] responseData = new byte[bytesRead];
                            readBuffer.get(responseData);
                            receivedData.append(new String(responseData, 0, readBuffer.position()));
                            readBuffer.clear();
                        }
                        // TODO Deal with the receivedData
                        String receivedMessage = receivedData.toString();

                        // Register write interest
                        String response = "Response from server";
                        socketChannel.register(selector, SelectionKey.OP_WRITE, ByteBuffer.wrap(response.getBytes()));
                        if (bytesRead == -1) {
                            // Close socket if the client was already disconnected
                            log.info("The client has been disconnected");
                            socketChannel.close();
                            clientDataMap.remove(socketChannel);
                        }
                        receivedData.setLength(0);
                    } else if (key.isValid() && key.isWritable()) {
                        // If current events are OP_WRITE, write message to client
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer responseBuffer = (ByteBuffer) key.attachment();
                        socketChannel.write(responseBuffer);

                        if (!responseBuffer.hasRemaining()) {
                            // All data sent, close connection or change interest to read
//                            socketChannel.close();
                            responseBuffer.clear();
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }

                    // Remove the current event to avoid repeat processing.
//                    iterator.remove();
//                    key.cancel();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
