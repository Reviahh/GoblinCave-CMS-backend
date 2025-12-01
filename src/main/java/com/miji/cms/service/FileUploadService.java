package com.miji.cms.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    /**
     * 上传视频文件
     */
    String uploadVideo(MultipartFile file);

    /**
     * 上传图片文件
     */
    String uploadImage(MultipartFile file);

    /**
     * 上传通用文件（PDF/ZIP/DOCX 等）
     */
    String uploadFile(MultipartFile file);
}
