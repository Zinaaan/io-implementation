package netty.upload;

import lombok.Data;

/**
 * @author lzn
 * @date 2023/06/21 10:06
 * @description
 */
@Data
public class FileDTO {

    private String fileName;

    /**
     * 1: create, 2, upload/write
     */
    private Integer command;

    /**
     * File data bytes. Better to use Asymmetric encryption to ensure the transmission information secure
     */
    private byte[] bytes;
}
