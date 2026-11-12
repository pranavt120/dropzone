package com.flashsale.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class IdempotencyInterceptor implements HandlerInterceptor {
    private static final String IDEMPOTENCY_KEY_HEADER = "X-idempotency-key";
    private static final String IDEMPOTENCY_KEY_ATTR = "idempotency-key";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if(idempotencyKey == null || idempotencyKey.trim().isEmpty()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Idempotency-key header is required");
            return false;
        }

        request.setAttribute(IDEMPOTENCY_KEY_ATTR, idempotencyKey);

        // add to mdc for logging/tracing
        MDC.put(IDEMPOTENCY_KEY_HEADER, idempotencyKey);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(IDEMPOTENCY_KEY_HEADER);
    }
}
