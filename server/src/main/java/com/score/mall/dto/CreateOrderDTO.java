package com.score.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderDTO {
    @NotNull(message = "商品不能为空")
    private Long productId;
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
    /** 收货地址ID (实物商品必填) */
    private Long addressId;
    /** 支付方式: POINTS_ONLY 纯积分 / POINTS_CASH 积分+现金 */
    @NotBlank(message = "支付方式不能为空")
    private String payType;
    private String remark;
}
