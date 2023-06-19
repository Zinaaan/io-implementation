package bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lzn
 * @date 2023/06/18 16:42
 * @description The origin implementation of BIO (Blocking IO)
 *
 * The server only use one thread to receive and handle client sockets.
 * If current socket is still in processing, the subsequent socket will be blocked, and access to process until the current socket completed.
 */
public class BioV1 {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9001);

            System.out.println("Waiting for client connection..............");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connection established...");
            handler(clientSocket);
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
        System.out.println("Start reading.........");
        try {
            int read = clientSocket.getInputStream().read();
            System.out.println("Finished reading.........");
            if(read != -1){
                System.out.println("Received the client data: " + new String(bytes, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
