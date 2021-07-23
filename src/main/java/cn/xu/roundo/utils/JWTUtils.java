package cn.xu.roundo.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JWTUtils {

    private static final Logger log = LoggerFactory.getLogger(JWTUtils.class);
    /**
     * token 过期时间, 单位: 秒. 这个值表示 30 天
     */
    private static final long TOKEN_EXPIRED_TIME = 30 * 24 * 60 * 60;

    private static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String createToken(Map<String, Object> data) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId(String.valueOf(data.get("user_id")))
                .setSubject(String.valueOf(data.get("user_account")))    //用户名
                .setIssuedAt(new Date())//登录时间
                .signWith(key)
                .setExpiration(new Date(new
                        Date().getTime() + 86400000));
        //设置过期时间
        //前三个为载荷playload 最后一个为头部 header
        System.out.println(jwtBuilder.compact());
        return jwtBuilder.compact();
    }

    /**
     * 验证jwt
     */
    public static Claims verifyJwt(String token) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        log.info(String.valueOf(body));
        return body;
    }

}