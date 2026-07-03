package com.resumepilot.jd.controller;

import com.resumepilot.common.result.Result;
import com.resumepilot.jd.service.JdService;
import com.resumepilot.jd.vo.JdVO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jd")
@RequiredArgsConstructor
public class JdController {

    private final JdService jdService;

    /** 提交 JD 匹配（异步） */
    @PostMapping("/match")
    public Result<JdVO> match(@RequestBody JdMatchRequest request) {
        JdVO vo = jdService.submit(request.getResumeId(), request.getJdText());
        return Result.success(vo);
    }

    /** 查询匹配结果（前端轮询用） */
    @GetMapping("/{id}")
    public Result<JdVO> getResult(@PathVariable Long id) {
        return Result.success(jdService.getResult(id));
    }

    /** 获取当前用户的匹配记录 */
    @GetMapping("/my")
    public Result<List<JdVO>> getMyRecords() {
        return Result.success(jdService.getMyRecords());
    }

    @Data
    public static class JdMatchRequest {
        private Long resumeId;
        @NotBlank(message = "岗位描述不能为空")
        private String jdText;
    }
}
