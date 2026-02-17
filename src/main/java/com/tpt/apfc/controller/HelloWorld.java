package com.tpt.apfc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorld {

    @GetMapping("/")
    public String anonymous() {
        System.out.println("Hello Anonymous !!!");
        return "Hello Anonymous !!!";
    }

    @GetMapping("/helloworld")
    public String helloWorld() {

        System.out.println("Hello World !!!");

        return "Hello World!!!";
    }



}