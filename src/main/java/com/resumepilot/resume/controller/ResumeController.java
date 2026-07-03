package com.resumepilot.resume.controller;

import com.resumepilot.common.result.Result;
import com.resumepilot.resume.service.ResumeService;
import com.resumepilot.resume.vo.ResumeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简历 Controller
 *
 * POST /resume/upload       → 上传文件，立即返回（异步解析）
 * GET  /resume/{id}         → 查询单个简历详情
 * GET  /resume/my           → 查询当前用户的所有简历（用于下拉选择）
 */
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public Result<ResumeVO> upload(@RequestParam("file") MultipartFile file) {
        ResumeVO vo = resumeService.upload(file);
        return Result.success(vo);
    }

    @GetMapping("/{id}")
    public Result<ResumeVO> getById(@PathVariable Long id) {
        ResumeVO vo = resumeService.getResumeById(id);
        return Result.success(vo);
    }

    /**
     * 获取当前用户的所有简历列表
     * 前端 ATS / JD 页面用下拉框选择简历，不用手动输入 ID
     */
    @GetMapping("/my")
    public Result<List<ResumeVO>> getMyResumes() {
        List<ResumeVO> list = resumeService.getMyResumes();
        return Result.success(list);
    }
}
