package cn.xu.roundo.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.xu.roundo.response.Response;
import cn.xu.roundo.service.IConfService;
import cn.xu.roundo.utils.Constants;
import cn.xu.roundo.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/common")
public class Common {

    @Autowired
    IConfService confService;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    RedisUtil redis;

    @GetMapping("/sendMail/{mail}")
    public Object sendEmail(@PathVariable("mail") String mail) {
        if (!ReUtil.isMatch("^\\w+@\\w+.\\w+", mail)) {
            return new Response(400, "邮箱格式错误！");
        }
        if (redis.getCacheObject(Constants.mailCode(mail)) != null) {
            return new Response(400, "不要重复发送验证码！");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("xjx_me@qq.com");
        message.setTo(mail);
        message.setSubject("验证码");
        int code = RandomUtil.randomInt(100000, 999999);
        message.setText("临时验证码【" + code + "】有效期为1分钟");
        javaMailSender.send(message);
        redis.setCacheObject(Constants.mailCode(mail), code, 60, TimeUnit.SECONDS);
        return new Response(200, "邮箱发送成功");
    }
}
