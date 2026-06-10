package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("movies")
public class Movie {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String poster;
    private String genre;
    private Integer duration;
    private BigDecimal rating;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
