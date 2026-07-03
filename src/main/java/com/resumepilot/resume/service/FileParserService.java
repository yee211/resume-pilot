package com.resumepilot.resume.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import org.apache.tika.exception.TikaException;

/**
 * 文件解析服务
 * 负责从 PDF / DOCX 中提取纯文本
 *
 * 用 Apache Tika：一个"万能文件解析器"，支持 PDF、Word、Excel、PPT 等
 * 你不需要关心文件格式差异，Tika 帮你统一处理
 */
@Slf4j
@Service
public class FileParserService {

    private final Tika tika = new Tika();

    /**
     * 从上传文件中提取文本
     *
     * @param file 上传的文件
     * @return 提取出的纯文本
     */
    public String extractText(MultipartFile file) {
        try {
            String text = tika.parseToString(file.getInputStream());
            log.info("文件解析成功，文件名：{}，文本长度：{}", file.getOriginalFilename(), text.length());
            return text;
        } catch (IOException | TikaException e) {
            log.error("文件解析失败，文件名：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件解析失败：" + e.getMessage());
        }
    }
}
