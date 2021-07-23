package cn.xu.roundo.controller;

import cn.hutool.jwt.JWT;

public class BaseController {
    public JWT verify(String token) {
        JWT jwt = JWT.of(token);
        if (!jwt.verify()) {
            return null;
        }
        return jwt;
    }
}


