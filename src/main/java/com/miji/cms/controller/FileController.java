package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.Submission;
import com.miji.cms.service.FileUploadService;
import com.miji.cms.service.SubmissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件接口
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5173/","http://localhost:3000/","https://miji-frontend.vercel.app/"},allowCredentials = "true")
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileUploadService fileUploadService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Resource
    private SubmissionService submissionService;


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
     * 图片上传
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

    /**
     * 文件下载
     * @param submissionId
     * @param response
     */
    @GetMapping("/download")
    public void downloadFile(
            @RequestParam Long submissionId,
            HttpServletResponse response) {

        Submission submission = submissionService.getById(submissionId);
        if (submission == null || submission.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交不存在");
        }

        String fileUrl = submission.getFileUrl();
        if (fileUrl == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交记录没有文件");
        }

        // uploadPath 示例：D:/cms/uploads
        String basePath = uploadPath;

        // fileUrl 示例：/uploads/files/xx.pdf
        String relativePath = fileUrl.replaceFirst("/uploads/", "");   // => files/xx.pdf
        File file = new File(basePath, relativePath);

        if (!file.exists()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不存在");
        }

        try {
            // 避免影响之前 header
            response.reset();

            // 强制下载，不看类型
            response.setContentType("application/octet-stream");
            response.setContentLengthLong(file.length());

            // 解决下载中文文件名乱码
            String fileName = file.getName();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            // 多浏览器兼容
            String contentDisposition = String.format(
                    "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                    encodedFileName, encodedFileName
            );
            response.setHeader("Content-Disposition", contentDisposition);

            // 输出文件流
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                 ServletOutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        }
    }

}
