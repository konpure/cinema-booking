package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("screenings")
public class Screening {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long movieId;
    private Long cinemaId;
    private String hallName;
    private LocalDateTime startTime;
    private BigDecimal price;
    private Integer seatRows;
    private Integer seatCols;
    private String status;
}
