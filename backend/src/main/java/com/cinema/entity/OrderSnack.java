package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_snacks")
public class OrderSnack {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long snackId;
    private String snackName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
