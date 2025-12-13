package com.lls.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云OSS服务接口
 */
public interface OssService {
    /**
     * 上传文件到OSS
     */
    String uploadFile(MultipartFile file);
}

