package com.miji.cms.service.impl;

import com.miji.cms.exception.BusinessException;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public String uploadVideo(MultipartFile file) {
        return uploadFileInternal(file, "videos");
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return uploadFileInternal(file, "images");
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return uploadFileInternal(file, "files");
    }

    private String uploadFileInternal(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(uploadPath + File.separator + folder + File.separator + fileName);
        dest.getParentFile().mkdirs();

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }

        // 返回可访问 URL
        return "/uploads/" + folder + "/" + fileName;
    }
}
