package netty.chat;

import netty.chat.server.DiscardServer;

/**
 * @author lzn
 * @date 2023/06/18 22:24
 * @description
 */
public class Start {

    public static void main(String[] args) {
        new DiscardServer(9001).run();
    }
}
