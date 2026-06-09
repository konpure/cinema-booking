package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("order_seats")
public class OrderSeat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Integer rowNum;
    private Integer colNum;
}
