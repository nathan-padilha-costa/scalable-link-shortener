package com.linkshortener.demo.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.linkshortener.demo.model.LinkRepository;
import com.linkshortener.demo.model.Link;

import java.util.List;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LinkSyncTask {
    private final LinkRepository linkRepository;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void syncCountsToDatabase(){

        List<String> dirtyLinks = redisTemplate.opsForSet().pop("dirty_links", 100);

        if (dirtyLinks == null || dirtyLinks.isEmpty()){
            return;
        }

        

        for (String shortCode : dirtyLinks) {
            String countStr = redisTemplate.opsForValue().get("count:" + shortCode);

            if (countStr != null) {
                Long redisCount = Long.parseLong(countStr);

                Link link = linkRepository.findById(shortCode).orElse(null);

                if (link != null) {
                    if (redisCount > link.getClickCount()) {
                        link.setClickCount(redisCount);
                        linkRepository.save(link);
                    }
                }

                

            }
        }


    }
    
}
