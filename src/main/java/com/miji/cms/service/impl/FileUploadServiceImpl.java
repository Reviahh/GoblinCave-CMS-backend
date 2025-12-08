package com.miji.cms.service.impl;

import com.miji.cms.exception.BusinessException;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload-path}")
    private String uploadPath;

    // 支持的视频格式
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv");
    
    // 支持的图片格式
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    
    // 支持的文档和压缩包格式
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            "zip", "rar", "7z", "tar", "gz"
    );

    @Override
    public String uploadVideo(MultipartFile file) {
        validateFileExtension(file, VIDEO_EXTENSIONS, "视频");
        return uploadFileInternal(file, "videos");
    }

    @Override
    public String uploadImage(MultipartFile file) {
        validateFileExtension(file, IMAGE_EXTENSIONS, "图片");
        return uploadFileInternal(file, "images");
    }

    @Override
    public String uploadFile(MultipartFile file) {
        validateFileExtension(file, DOCUMENT_EXTENSIONS, "文档或压缩包");
        return uploadFileInternal(file, "files");
    }

    /**
     * 验证文件扩展名
     */
    private void validateFileExtension(MultipartFile file, List<String> allowedExtensions, String fileType) {
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    "不支持的" + fileType + "格式，仅支持: " + String.join(", ", allowedExtensions));
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 内部文件上传方法
     */
    private String uploadFileInternal(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "_" + 
                         System.currentTimeMillis() + "." + extension;
        
        // 构建目标路径
        File dest = new File(uploadPath + File.separator + folder + File.separator + fileName);
        
        // 确保目录存在
        if (!dest.getParentFile().exists()) {
            boolean mkdirs = dest.getParentFile().mkdirs();
            if (!mkdirs) {
                log.error("创建上传目录失败: {}", dest.getParentFile().getAbsolutePath());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建上传目录失败");
            }
        }

        try {
            // 保存文件
            file.transferTo(dest);
            log.info("文件上传成功: {}", dest.getAbsolutePath());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }

        // 返回可访问 URL
        return "/uploads/" + folder + "/" + fileName;
    }
}
