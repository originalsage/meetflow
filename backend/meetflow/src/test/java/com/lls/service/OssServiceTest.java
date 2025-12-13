package com.lls.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * OSS服务测试类
 */
@SpringBootTest
@Slf4j
class OssServiceTest {

    @Autowired
    private OssService ossService;

    @Test
    void testUploadFile() {
        String filePath = "E:\\develop\\photoshop\\photos\\大炮.png";
        File file = new File(filePath);

        if (!file.exists()) {
            log.error("文件不存在: {}", filePath);
            return;
        }

        try {
            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(file.toPath());
            
            // 创建MockMultipartFile
            MultipartFile multipartFile = new MockMultipartFile(
                    "file",                    // 参数名
                    file.getName(),            // 文件名
                    "image/png",               // 内容类型
                    fileContent                // 文件内容
            );

            // 执行上传
            log.info("开始上传文件: {}", file.getName());
            String url = ossService.uploadFile(multipartFile);
            
            log.info("上传成功！文件URL: {}", url);
            
        } catch (IOException e) {
            log.error("读取文件失败", e);
        } catch (Exception e) {
            log.error("上传文件失败", e);
        }
    }
}

