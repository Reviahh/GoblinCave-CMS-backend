package com.miji.cms.service.impl;

import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FileUploadServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文件上传服务测试")
class FileUploadServiceImplTest {

    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    @Mock
    private MultipartFile mockFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileUploadService, "uploadPath", tempDir.toString());
    }

    @Nested
    @DisplayName("视频上传测试")
    class VideoUploadTests {

        @Test
        @DisplayName("成功上传MP4视频")
        void testUploadVideo_MP4_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_video.mp4");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadVideo(mockFile);

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/videos/"));
            assertTrue(result.endsWith(".mp4"));
        }

        @Test
        @DisplayName("成功上传AVI视频")
        void testUploadVideo_AVI_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_video.avi");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadVideo(mockFile);

            assertNotNull(result);
            assertTrue(result.endsWith(".avi"));
        }

        @Test
        @DisplayName("成功上传MOV视频")
        void testUploadVideo_MOV_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_video.mov");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadVideo(mockFile);

            assertNotNull(result);
            assertTrue(result.endsWith(".mov"));
        }

        @Test
        @DisplayName("上传不支持的视频格式失败")
        void testUploadVideo_UnsupportedFormat() {
            // validateFileExtension 先检查文件名，再检查扩展名
            when(mockFile.getOriginalFilename()).thenReturn("test_video.exe");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadVideo(mockFile));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("上传空文件名视频失败")
        void testUploadVideo_EmptyFilename() {
            when(mockFile.getOriginalFilename()).thenReturn("");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadVideo(mockFile));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("上传null文件名视频失败")
        void testUploadVideo_NullFilename() {
            when(mockFile.getOriginalFilename()).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadVideo(mockFile));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("图片上传测试")
    class ImageUploadTests {

        @Test
        @DisplayName("成功上传JPG图片")
        void testUploadImage_JPG_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_image.jpg");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadImage(mockFile);

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/images/"));
            assertTrue(result.endsWith(".jpg"));
        }

        @Test
        @DisplayName("成功上传PNG图片")
        void testUploadImage_PNG_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_image.png");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadImage(mockFile);

            assertNotNull(result);
            assertTrue(result.endsWith(".png"));
        }

        @Test
        @DisplayName("上传不支持的图片格式失败")
        void testUploadImage_UnsupportedFormat() {
            when(mockFile.getOriginalFilename()).thenReturn("test_image.tiff");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadImage(mockFile));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("文档上传测试")
    class DocumentUploadTests {

        @Test
        @DisplayName("成功上传PDF文档")
        void testUploadFile_PDF_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_document.pdf");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadFile(mockFile);

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/files/"));
            assertTrue(result.endsWith(".pdf"));
        }

        @Test
        @DisplayName("成功上传Word文档")
        void testUploadFile_DOCX_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_document.docx");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadFile(mockFile);

            assertNotNull(result);
            assertTrue(result.endsWith(".docx"));
        }

        @Test
        @DisplayName("成功上传ZIP压缩包")
        void testUploadFile_ZIP_Success() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test_archive.zip");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadFile(mockFile);

            assertNotNull(result);
            assertTrue(result.endsWith(".zip"));
        }

        @Test
        @DisplayName("上传不支持的文档格式失败")
        void testUploadFile_UnsupportedFormat() {
            when(mockFile.getOriginalFilename()).thenReturn("test_file.exe");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadFile(mockFile));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("通用文件上传异常测试")
    class FileUploadExceptionTests {

        @Test
        @DisplayName("上传空文件失败")
        void testUploadFile_EmptyFile() {
            // 先通过 validateFileExtension（需要有效的文件名和扩展名）
            // 然后在 uploadFileInternal 中检查 isEmpty
            when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
            when(mockFile.isEmpty()).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadFile(mockFile));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("文件传输IO异常")
        void testUploadFile_IOExceptionDuringTransfer() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
            when(mockFile.isEmpty()).thenReturn(false);
            doThrow(new IOException("模拟IO异常")).when(mockFile).transferTo(any(File.class));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadFile(mockFile));
            assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("测试文件名中没有扩展名")
        void testUploadFile_NoExtension() {
            // getFileExtension 返回空字符串，不在 DOCUMENT_EXTENSIONS 列表中
            when(mockFile.getOriginalFilename()).thenReturn("testfile");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileUploadService.uploadFile(mockFile));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("文件名生成测试")
    class FileNameGenerationTests {

        @Test
        @DisplayName("生成的文件名唯一性测试")
        void testUploadFile_UniqueFileName() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result1 = fileUploadService.uploadFile(mockFile);
            String result2 = fileUploadService.uploadFile(mockFile);

            assertNotEquals(result1, result2, "两次上传应该生成不同的文件名");
        }

        @Test
        @DisplayName("生成的文件名包含UUID和时间戳")
        void testUploadFile_FileNameFormat() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("original_name.pdf");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadFile(mockFile);

            assertNotNull(result);
            String fileName = result.substring(result.lastIndexOf("/") + 1);
            assertTrue(fileName.contains("_"));
            assertTrue(fileName.endsWith(".pdf"));
        }
    }

    @Nested
    @DisplayName("路径构建测试")
    class PathBuildingTests {

        @Test
        @DisplayName("视频上传路径正确")
        void testVideoUploadPath() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("video.mp4");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadVideo(mockFile);

            assertTrue(result.contains("/videos/"));
        }

        @Test
        @DisplayName("图片上传路径正确")
        void testImageUploadPath() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("image.png");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadImage(mockFile);

            assertTrue(result.contains("/images/"));
        }

        @Test
        @DisplayName("文档上传路径正确")
        void testDocumentUploadPath() throws IOException {
            when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
            when(mockFile.isEmpty()).thenReturn(false);
            doNothing().when(mockFile).transferTo(any(File.class));

            String result = fileUploadService.uploadFile(mockFile);

            assertTrue(result.contains("/files/"));
        }
    }
}