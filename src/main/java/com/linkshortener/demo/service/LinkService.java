package com.linkshortener.demo.service;


import java.util.Optional;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.linkshortener.demo.model.Link;
import com.linkshortener.demo.model.LinkRepository;
import com.linkshortener.demo.dto.LinkAnalytics;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final StringRedisTemplate redisTemplate;

    private final ShortCodeGenerator shortCodeGenerator;



    public Link createShortLink(String longUrl) {

        String shortCode = shortCodeGenerator.generate();

        Link newLink = new Link();
        newLink.setShortCode(shortCode);
        newLink.setLongURL(longUrl);

        Link savedLink = linkRepository.save(newLink);

        redisTemplate.opsForValue().set(shortCode, longUrl, 10, TimeUnit.MINUTES);

        return savedLink;

    }
    
    @Transactional
    public Optional<String> getOriginalUrl(String shortCode) {

        String cachedUrl =redisTemplate.opsForValue().get(shortCode);

        if (cachedUrl != null){

            redisTemplate.opsForValue().increment("count:" + shortCode);
            redisTemplate.opsForSet().add("dirty_links", shortCode);


            
            return Optional.of(cachedUrl);
        }
        
        Optional<Link> linkOptional = linkRepository.findById(shortCode);

        if (linkOptional.isPresent()){
            Link link = linkOptional.get();

            redisTemplate.opsForValue().set(shortCode, link.getLongURL(), 10, TimeUnit.MINUTES);

            redisTemplate.opsForValue().set("count:" + shortCode, String.valueOf(link.getClickCount()+1));

            linkRepository.save(link);

            return Optional.of(link.getLongURL());
        }

        return Optional.empty();

    }

    public LinkAnalytics getAnalytics (String shortCode){

        Link link = linkRepository.findById(shortCode).orElseThrow(()-> new RuntimeException("Link not found"));

        String redisCount = redisTemplate.opsForValue().get("count:" + shortCode);

        Long totalClicks;
        if (redisCount != null){
            totalClicks = Long.parseLong(redisCount);
        }
        else {
            totalClicks = link.getClickCount();
         }

        return new LinkAnalytics(link.getShortCode(), link.getLongURL(), totalClicks);


    }
}
