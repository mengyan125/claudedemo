package com.feedback.controller;

import com.feedback.common.exception.BusinessException;
import com.feedback.common.result.Result;
import com.feedback.mapper.FbFeedbackAttachmentMapper;
import com.feedback.model.entity.FbFeedbackAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 文件上传控制器
 * 处理反馈附件的文件上传
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    /** 允许的文件扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "png", "gif", "mp4", "avi", "mov")
    );

    /** 最大文件大小：25MB */
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    private FbFeedbackAttachmentMapper fbFeedbackAttachmentMapper;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @return 包含附件ID、文件名、文件URL的响应
     */
    @PostMapping("/file")
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        // 校验文件
        validateFile(file);
        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String storedName = UUID.randomUUID().toString() + "." + extension;
        // 保存文件到磁盘
        saveFileToDisk(file, storedName);
        // 插入附件记录
        FbFeedbackAttachment attachment = createAttachmentRecord(originalName, storedName,
                extension, file.getSize());
        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("id", attachment.getId());
        data.put("fileName", originalName);
        data.put("fileUrl", "/api/files/" + storedName);
        return Result.ok(data);
    }

    /** 校验文件类型和大小 */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过25MB");
        }
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("不支持的文件类型，仅允许：jpg, png, gif, mp4, avi, mov");
        }
    }

    /** 获取文件扩展名 */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /** 保存文件到磁盘 */
    private void saveFileToDisk(MultipartFile file, String storedName) {
        File dir = new File(uploadDir).getAbsoluteFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, storedName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /** 创建附件数据库记录 */
    private FbFeedbackAttachment createAttachmentRecord(String originalName, String storedName,
                                                         String fileType, long fileSize) {
        FbFeedbackAttachment attachment = new FbFeedbackAttachment();
        attachment.setFeedbackId(null);
        attachment.setFileName(originalName);
        attachment.setFilePath(storedName);
        attachment.setFileType(fileType);
        attachment.setFileSize(fileSize);
        attachment.setCreateTime(new Date());
        fbFeedbackAttachmentMapper.insert(attachment);
        return attachment;
    }
}
