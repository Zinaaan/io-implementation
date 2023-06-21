package netty.upload.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netty.upload.FileDTO;

import java.io.*;

/**
 * @author lzn
 * @date 2023/06/18 22:42
 * @description File upload server handler
 */
public class UploadFileHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client connected.........");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileDTO) {
            FileDTO fileDTO = (FileDTO) msg;
            if (fileDTO.getCommand() == 1) {
                // Create file
                File file = new File(fileDTO.getFileName());
                if (!file.exists()) {
                    boolean isCreateSuccess = file.createNewFile();
                    if(!isCreateSuccess){
                        throw new RuntimeException("Create new file error");
                    }
                }
            } else if (fileDTO.getCommand() == 2) {
                // Upload/write file
                boolean success = saveToFile(fileDTO.getFileName(), fileDTO.getBytes());
                if(!success){
                    throw new RuntimeException("Write/upload file error");
                }
            }
        }
    }

    public static boolean saveToFile(String filename, byte[] msg) {
        OutputStream fos = null;
        try {
            File file = new File(filename);
            File parent = file.getParentFile();
            if ((!parent.exists()) & (!parent.mkdirs())) {
                return false;
            }
            fos = new FileOutputStream(file, true);
            fos.write(msg);
            fos.flush();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
