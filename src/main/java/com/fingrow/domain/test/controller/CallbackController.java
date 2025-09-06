package com.fingrow.domain.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "카카오 콜백", description = "카카오 OAuth 콜백 처리")
@Controller
@Slf4j
public class CallbackController {

    @Operation(summary = "카카오 OAuth 콜백", description = "카카오 로그인 후 리다이렉트되는 콜백을 처리합니다.")
    @GetMapping("/callback")
    public String handleKakaoCallback(@RequestParam(value = "code", required = false) String code,
                                    @RequestParam(value = "error", required = false) String error,
                                    @RequestParam(value = "state", required = false) String state) {
        
        log.info("카카오 콜백 처리 - code: {}, error: {}, state: {}", code, error, state);
        
        if (error != null) {
            log.error("카카오 로그인 오류: {}", error);
            return "redirect:/test/kakao-login.html?error=" + error;
        }
        
        if (code != null) {
            log.info("카카오 인증 코드 수신: {}", code);
            return "redirect:/test/kakao-login.html?code=" + code;
        }
        
        log.warn("카카오 콜백에서 코드나 오류를 받지 못함");
        return "redirect:/test/kakao-login.html";
    }
}