package com.atguigui.gmall.auth.controller;


import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigui.gmall.auth.config.JwtProperties;
import com.atguigui.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredit")
    public Resp<Object> accredit(@RequestParam("username")String userName,
                                 @RequestParam("password")String password,
                                 HttpServletRequest request, HttpServletResponse response){
        String token = this.authService.accredit(userName, password);
        CookieUtils.setCookie(request,response,this.jwtProperties.getCookieName(),token,this.jwtProperties.getExprieTime()*60);
        return Resp.ok(null);
    }

}
