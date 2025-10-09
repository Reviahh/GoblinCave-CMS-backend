package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.FileUpload;
import com.miji.cms.service.FileUploadService;
import com.miji.cms.mapper.FileUploadMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【file_upload(文件上传表)】的数据库操作Service实现
* @createDate 2025-10-09 15:09:19
*/
@Service
public class FileUploadServiceImpl extends ServiceImpl<FileUploadMapper, FileUpload>
    implements FileUploadService{

}




