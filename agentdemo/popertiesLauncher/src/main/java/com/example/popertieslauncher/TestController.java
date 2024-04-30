package com.example.popertieslauncher;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wzg
 * @date 2024/4/11 22:51
 */
@RequestMapping("/test")
@RestController
public class TestController {
    @GetMapping("/info")
    @ResponseBody
    public String hello(){
        return  "test";
    }
}
