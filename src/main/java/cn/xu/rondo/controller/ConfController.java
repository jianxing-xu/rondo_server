package cn.xu.rondo.controller;


import cn.xu.rondo.interceptor.VisitorInter;
import cn.xu.rondo.service.IConfService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;

/**
 * <p>
 * 配置表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Controller
@RequestMapping("/conf")
public class ConfController extends BaseController {
    @Autowired
    IConfService confService;

    @VisitorInter
    @GetMapping("/conf/{key}")
    public String getConf(@PathVariable("key") String key) {
        return get(key);
    }
}

