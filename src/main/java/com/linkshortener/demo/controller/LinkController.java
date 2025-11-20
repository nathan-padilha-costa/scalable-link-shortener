package com.linkshortener.demo.controller;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.linkshortener.demo.dto.LinkAnalytics;
import com.linkshortener.demo.dto.ShortenRequest;
import com.linkshortener.demo.model.Link;
import com.linkshortener.demo.service.LinkService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    public Link createShortLink (@Valid @RequestBody ShortenRequest request){
        return linkService.createShortLink(request.getLongUrl());
    }
    

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {

        Optional<String> urlOptional = linkService.getOriginalUrl(shortCode);

        if (urlOptional.isPresent()){

            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(urlOptional.get())).build();
        }

        else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/shorten/{shortCode}/stats")
    public LinkAnalytics getLinkStats (@PathVariable String shortCode){
        return linkService.getAnalytics(shortCode);
    }
}
