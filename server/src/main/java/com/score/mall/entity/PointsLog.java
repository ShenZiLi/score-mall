package com.score.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_log")
public class PointsLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer changePoints;
    private Integer beforePoints;
    private Integer afterPoints;
    /** SIGN/ORDER_EARN/EXCHANGE/REFUND/ADMIN */
    private String type;
    private Long relatedId;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
