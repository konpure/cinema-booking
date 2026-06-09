package com.cinema.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SnackOrderItem {
    @NotNull
    private Long snackId;
    @NotNull
    @Min(1)
    private Integer quantity;
}
