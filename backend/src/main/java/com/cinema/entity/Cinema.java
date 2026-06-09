package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cinemas")
public class Cinema {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String city;
    private String address;
    private String phone;
    private String cover;
    private String status;
}
