package com.leyou.upload.service.impl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    private static final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");

    public String uploadImage(MultipartFile file){
        try {
            // 校验文件
            //1, 校验文件类型
            String contentType = file.getContentType();
            if(!ALLOW_CONTENT_TYPES.contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //2, 校验文件内容 如果不是图片就直接报错，会是null；或者读取图片的长宽高。
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image  == null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 保存到本地
//            File dest = new File("/usr/local/var/www", file.getOriginalFilename());
//            file.transferTo(dest);

            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            //3 上传到FastDFS
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            // 返回地址
            String url = "http://image.leyou.com/" + storePath.getFullPath();
            return url;
        }catch (Exception e){
            log.error("【文件上传服务】 文件上传失败！",e);
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}
