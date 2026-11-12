package com.flashsale.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 2, message = "Quantity cannot be more than 2")
        int quantity,

        @NotNull(message = "userId cannot be null")
        String userId
) {
}
