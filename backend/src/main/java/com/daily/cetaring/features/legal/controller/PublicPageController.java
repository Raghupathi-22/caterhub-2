package com.daily.cetaring.features.legal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicPageController {

    @GetMapping("/delete-account")
    public String deleteAccountPage() {

        return "forward:/delete-account.html";
    }
}
