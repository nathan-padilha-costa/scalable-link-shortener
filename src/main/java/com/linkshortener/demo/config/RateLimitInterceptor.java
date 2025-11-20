package com.linkshortener.demo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.linkshortener.demo.service.RateLimitService;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor{

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getRequestURI().equals("/api/v1/shorten") && "POST".equalsIgnoreCase(request.getMethod())) {

            String ipAdress = request.getRemoteAddr();

            if (!rateLimitService.allowRequest(ipAdress)){
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded. Wait");
                return false;
            }

            
        }
        return true;
    }

    
}
