package com.resumepilot.career.vo;

import lombok.Data;

/**
 * 话术生成结果 VO
 */
@Data
public class CareerVO {

    /** Boss 直聘话术 */
    private String boss;

    /** 智联招聘话术 */
    private String zhilian;

    /** 牛客网话术 */
    private String niuke;

    /** 邮件投递话术 */
    private String email;
}
