package com.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatPosition {
    @NotNull
    private Integer row;
    @NotNull
    private Integer col;
}
