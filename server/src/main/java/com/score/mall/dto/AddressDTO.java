package com.score.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    @NotBlank(message = "收货人不能为空")
    private String name;
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
    @NotBlank(message = "省不能为空")
    private String province;
    @NotBlank(message = "市不能为空")
    private String city;
    @NotBlank(message = "区不能为空")
    private String district;
    @NotBlank(message = "详细地址不能为空")
    private String detail;
    private Integer isDefault;
}
