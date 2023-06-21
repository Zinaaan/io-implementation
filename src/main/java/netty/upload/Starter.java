package netty.upload;

import netty.upload.server.UploadFileServer;

/**
 * @author lzn
 * @date 2023/06/18 22:24
 * @description
 */
public class Starter {

    public static void main(String[] args) {
        new UploadFileServer(9001).run();
    }
}
