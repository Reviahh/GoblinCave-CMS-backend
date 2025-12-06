package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.service.FileUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 文件接口
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5173/","http://localhost:3000/","https://miji-frontend.vercel.app/"},allowCredentials = "true")
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileUploadService fileUploadService;

    /**
     * 视频上传
     * @param file
     * @return
     */
    @PostMapping("/upload/video")
    public BaseResponse<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadVideo(file);
        return ResultUtils.success(url);
    }

    /**
     * 音频上传
     * @param file
     * @return
     */
    @PostMapping("/upload/image")
    public BaseResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        return ResultUtils.success(url);
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload/file")
    public BaseResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file);
        return ResultUtils.success(url);
    }
}
