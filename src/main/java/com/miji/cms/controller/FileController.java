package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件接口
 *
 * @author miji
 */
@RestController
@RequestMapping("/file")
@CrossOrigin(origins = {"http://localhost:5173/","http://localhost:3000/","https://miji-frontend.vercel.app/"},allowCredentials = "true")
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

    /**
     * 上传图片
     */
    @PostMapping("/upload/image")
    public BaseResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }

        try {
            // 创建保存目录
            String imageDir = uploadPath + "images/";
            File dir = new File(imageDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名（防止重名）
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(imageDir + fileName);

            // 保存文件
            file.transferTo(dest);

            // 构造访问URL
            String url = "/uploads/images/" + fileName;
            return ResultUtils.success(url);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败: " + e.getMessage());
        }
    }
}
