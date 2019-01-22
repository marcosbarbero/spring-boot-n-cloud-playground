package com.marcosbarbero.lab.sec.oauth.opaque.ds.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;

@RestController
@RequestMapping("/avengers")
public class DownstreamController {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<Collection<String>> get() {
        return ResponseEntity.ok(Arrays.asList("Iron man", "Spider-man", "Thor", "Hulk", "Cap", "Vision"));
    }

}
