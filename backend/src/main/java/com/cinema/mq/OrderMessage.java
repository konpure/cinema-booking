package com.cinema.mq;

import com.cinema.dto.SeatPosition;
import com.cinema.dto.SnackOrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {
    private Long userId;
    private Long screeningId;
    private Long cinemaId;
    private List<SeatPosition> seats;
    private List<SnackOrderItem> snacks;
    private String lockToken;
}
