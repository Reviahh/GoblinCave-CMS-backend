package com.miji.cms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VueRouterFallbackController {

    @RequestMapping({
            "/", "/news", "/courses", "/about", "/profile",
            "/admin/**", "/competitions", "/competitions/{id}",
            "/team-detail", "/my-competitions", "/recruitments",
            "/recruitments/{id}", "/submissions/{competitionId}",
            "/rankings/{competitionId}"
            // 可以只列出你路由中存在的路径，或者用通配
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}