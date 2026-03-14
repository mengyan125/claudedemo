package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 附件项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentItemVO {

    /** 附件ID */
    private Long id;

    /** 文件名 */
    private String fileName;

    /** 文件访问URL（从filePath转换） */
    private String fileUrl;

    /** 文件类型 */
    private String fileType;
}
