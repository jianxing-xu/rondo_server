package cn.xu.rondo.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.Response;
import cn.xu.rondo.service.IConfService;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.RedisUtil;
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
public class CommonController extends BaseController {

    @Autowired
    IConfService confService;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    RedisUtil redis;

    @GetMapping("/sendMail/{mail}")
    public String sendEmail(@PathVariable("mail") String mail) {
        if (!ReUtil.isMatch("^\\w+@\\w+.\\w+", mail)) {
            throw ERR(EE.MAIL_FORMAT_ERR);//"邮箱格式错误"
        }
        if (redis.getCacheObject(Constants.mailCode(mail)) != null) {
            throw ERR(EE.OFTEN_MAIL);//"不要重复发送验证码！");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("xjx_me@qq.com");
        message.setTo(mail);
        message.setSubject("验证码");
        String code = RandomUtil.randomNumbers(6);
        System.out.println("==================MAIL+CODE:  " + code + "========================");
        message.setText("临时验证码【" + code + "】有效期为1分钟");
        redis.setCacheObject(Constants.mailCode(mail), code, 60 * 5, TimeUnit.SECONDS);
        javaMailSender.send(message);
        return "邮箱发送成功";
    }
}
