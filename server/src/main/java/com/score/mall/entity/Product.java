package com.score.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String coverImage;
    private String images;
    private String detail;
    private BigDecimal originalPrice;
    private Integer pointsPrice;
    private BigDecimal cashPrice;
    private Integer stock;
    private Integer sales;
    private Integer status;
    private Integer isHot;
    private Integer isRecommend;
    private Integer sort;
    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
