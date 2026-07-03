package com.resumepilot.ats.controller;

import com.resumepilot.ats.service.AtsService;
import com.resumepilot.ats.vo.AtsVO;
import com.resumepilot.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ats")
@RequiredArgsConstructor
public class AtsController {

    private final AtsService atsService;

    /** 提交 ATS 评分（异步） */
    @PostMapping("/analyze/{resumeId}")
    public Result<AtsVO> analyze(@PathVariable Long resumeId) {
        AtsVO vo = atsService.submit(resumeId);
        return Result.success(vo);
    }

    /** 查询评分结果（前端轮询用） */
    @GetMapping("/{id}")
    public Result<AtsVO> getResult(@PathVariable Long id) {
        return Result.success(atsService.getResult(id));
    }

    /** 获取当前用户的评分记录 */
    @GetMapping("/my")
    public Result<List<AtsVO>> getMyRecords() {
        return Result.success(atsService.getMyRecords());
    }
}
