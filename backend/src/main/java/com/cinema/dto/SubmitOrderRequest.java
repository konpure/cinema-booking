package com.cinema.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitOrderRequest {
    @NotNull
    private Long screeningId;
    @NotBlank
    private String lockToken;
    @NotEmpty
    @Valid
    private List<SeatPosition> seats;
    @Valid
    private List<SnackOrderItem> snacks;
}
