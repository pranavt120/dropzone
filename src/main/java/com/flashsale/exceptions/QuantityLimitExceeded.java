package com.flashsale.exceptions;

public class QuantityLimitExceeded extends RuntimeException {
    public QuantityLimitExceeded(String message) {
        super(message);
    }
}
