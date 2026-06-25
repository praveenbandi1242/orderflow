package com.orderflow.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String correlationId = MDC.get("correlationId");

        log.info("""
                
                ====================================================
                Incoming Request
                ====================================================
                Correlation Id : {}
                Method         : {}
                URI            : {}
                Client IP      : {}
                ====================================================
                """,
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        filterChain.doFilter(request, response);

        long endTime = System.currentTimeMillis();

        log.info("""
                
                ====================================================
                Outgoing Response
                ====================================================
                Correlation Id : {}
                Status         : {}
                Time Taken     : {} ms
                ====================================================
                """,
                correlationId,
                response.getStatus(),
                (endTime - startTime));
    }
}
