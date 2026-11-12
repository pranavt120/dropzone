package com.flashsale.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        int errorStatus,
        String message,
        LocalDateTime time
) {
}
