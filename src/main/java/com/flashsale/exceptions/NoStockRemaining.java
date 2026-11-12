package com.flashsale.exceptions;

public class NoStockRemaining extends RuntimeException {
    public NoStockRemaining(String message) {
        super(message);
    }
}
