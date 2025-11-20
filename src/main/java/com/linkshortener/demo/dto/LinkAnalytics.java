package com.linkshortener.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LinkAnalytics {
    private String shortCode;
    private String longUrl;
    private Long totalClicks;
    
}
