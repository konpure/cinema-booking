package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("snacks")
public class Snack {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private String description;
    private String image;
    private String status;
}
