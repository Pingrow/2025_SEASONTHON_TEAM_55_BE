package com.fingrow.domain.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestViewController {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id:your-kakao-client-id}")
    private String kakaoClientId;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("kakaoClientId", kakaoClientId);
        return "test/login";
    }

    @GetMapping("/callback")
    public String callbackPage(@RequestParam(required = false) String code, 
                              @RequestParam(required = false) String error,
                              Model model) {
        if (error != null) {
            model.addAttribute("error", error);
            return "test/error";
        }
        
        if (code != null) {
            model.addAttribute("authCode", code);
            return "test/callback";
        }
        
        return "redirect:/test/login";
    }

    @GetMapping("/success")
    public String successPage(@RequestParam(required = false) String token,
                             @RequestParam(required = false) String user,
                             Model model) {
        model.addAttribute("accessToken", token);
        model.addAttribute("userData", user);
        return "test/success";
    }
}