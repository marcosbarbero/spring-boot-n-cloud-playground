package com.marcosbarbero.lab.sec.oauth.jwt.ds.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;

@RestController
@RequestMapping("/avengers")
public class DownstreamController {

    @GetMapping
    public ResponseEntity<Collection<String>> get() {
        return ResponseEntity.ok(Arrays.asList("Iron man", "Spider-man", "Thor", "Hulk", "Cap", "Vision"));
    }

}
