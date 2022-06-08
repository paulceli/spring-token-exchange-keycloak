package com.celi.apib.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerB {

    @GetMapping("/api-b/hello")
    @ResponseStatus(HttpStatus.OK)
    public String hello(@RequestHeader Map<String, String> header) {
        System.out.println("API-B received request header : " + header);

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String user = jwt.getClaimAsString("preferred_username");

        return "Hello " + user + " from API-B";
    }
}
