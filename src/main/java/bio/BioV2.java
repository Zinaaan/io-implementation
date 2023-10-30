package bio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lzn
 * @date 2023/06/18 16:42
 * @description The optimized version of origin implementation of BIO (Blocking IO)
 * <p>
 * What's new
 * <p>
 * Old:
 * In V1, the server only use one thread to receive and handle client sockets.
 * If current socket is still in processing, the subsequent socket will be blocked, and access to process until the current socket completed.
 * <p>
 * New-1:
 * The server will open a separate thread to process each new socket so that the multiple socket could process concurrently.
 * <p>
 * New-2:
 * The server will create a thread pool to reduce the overhead of thread creation and destruction.
 */
@Slf4j
public class BioV2 {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9001);
            while (true) {
                log.info("Waiting for client connection..............");
                Socket clientSocket = serverSocket.accept();
                log.info("Connection established...");

                new Thread(() -> {
                    try {
                        handler(clientSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle client socket
     *
     * @param clientSocket: Socket received by server
     */
    private static void handler(Socket clientSocket) {
        byte[] bytes = new byte[1024];
        log.info("Start reading.........");
        try {
            int read = clientSocket.getInputStream().read();
            log.info("Finished reading.........");
            if (read != -1) {
                log.info("Received the client data: " + new String(bytes, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
