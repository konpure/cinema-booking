package com.cinema.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotification implements Serializable {
    private String orderNo;
    private Long userId;
    private Long screeningId;
}
