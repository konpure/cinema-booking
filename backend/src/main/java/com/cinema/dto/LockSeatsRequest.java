package com.cinema.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LockSeatsRequest {
    @NotNull
    private Long screeningId;
    @NotEmpty
    private List<SeatPosition> seats;
}
