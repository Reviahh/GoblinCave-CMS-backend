package com.miji.cms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 上传视频
     */
    @PostMapping("/upload/video")
    public String uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "文件不能为空";
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 拼接存储路径
        File dest = new File(uploadPath + File.separator + "videos" + File.separator + fileName);
        dest.getParentFile().mkdirs(); // 确保目录存在
        file.transferTo(dest); // 保存文件

        // 返回前端可访问的 URL
        return "http://localhost:8080/files/videos/" + fileName;
    }
}
