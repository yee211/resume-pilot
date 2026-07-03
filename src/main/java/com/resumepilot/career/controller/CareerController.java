package com.resumepilot.career.controller;

import com.resumepilot.career.dto.CareerRequest;
import com.resumepilot.career.service.CareerService;
import com.resumepilot.career.vo.CareerVO;
import com.resumepilot.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 求职话术 Controller
 *
 * POST /career/generate → 生成话术
 */
@RestController
@RequestMapping("/career")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    /**
     * 生成求职话术
     * 根据简历和目标职位，生成 Boss/智联/牛客/邮件不同风格的话术
     */
    @PostMapping("/generate")
    public Result<CareerVO> generate(@Valid @RequestBody CareerRequest request) {
        CareerVO vo = careerService.generate(request);
        return Result.success(vo);
    }
}
