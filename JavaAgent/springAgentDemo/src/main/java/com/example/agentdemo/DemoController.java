package com.example.agentdemo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wzg
 * @date 2024/4/11 22:51
 */
@RequestMapping("/hello")
@RestController
public class DemoController {
    @GetMapping("/info")
    @ResponseBody
    public String hello(){
        return  "Hello world";
    }
}
