package cn.xu.rondo.controller;


import cn.xu.rondo.entity.Conf;
import cn.xu.rondo.interceptor.VisitorInter;
import cn.xu.rondo.service.IConfService;
import cn.xu.rondo.utils.Common;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 配置表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@RestController
@RequestMapping("/conf")
public class ConfController extends BaseController {
    @Autowired
    IConfService confService;

    @VisitorInter
    @GetMapping("/conf/{key}")
    public String getConf(@PathVariable("key") String key) {
        return get(key);
    }
    @VisitorInter
    @GetMapping("/conf/{keys}")
    public Map<String, String> getConfForKeys(@PathVariable("keys") String keys) {
        final String[] keyList = keys.split(",");
        Map<String, String> m = new HashMap<>();
        for (String k : keyList) {
            String v = get(k);
            m.put(k, v);
        }
        return m;
    }

    @PostMapping("/add")
    public void add(@RequestParam(value = "conf_key") String key,
                    @RequestParam(value = "conf_value") String value,
                    @RequestParam(value = "conf_desc") String desc) {
        Conf conf = new Conf();
        conf.setConf_key(key);
        conf.setConf_value(value);
        conf.setConf_desc(desc);
        conf.setConf_createtime(Common.time().intValue());
        confService.save(conf);
    }

    @PostMapping("/update")
    public void update(@RequestParam(value = "conf_id") Integer id,
                       @RequestParam(value = "conf_key") String key,
                       @RequestParam(value = "conf_value") String value,
                       @RequestParam(value = "conf_desc") String desc) {
        Conf conf = new Conf();
        conf.setConf_key(key);
        conf.setConf_id(id);
        conf.setConf_value(value);
        conf.setConf_desc(desc);
        conf.setConf_updatetime(Common.time().intValue());
        confService.updateById(conf);
    }

    @PostMapping("/del")
    public void update(@RequestParam("id") Integer id) {
        confService.removeById(id);
    }

    @GetMapping("/all")
    public JSONObject allConf(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                              @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        final Page<Conf> confPager = new Page<>(pageNum, pageSize);
        final QueryWrapper<Conf> wrapper = new QueryWrapper<>();
        wrapper.like("conf_key", keyword).or();
        wrapper.like("conf_value", keyword).or();
        wrapper.like("conf_desc", keyword).or();
        final Page<Conf> page = confService.page(confPager, wrapper);
        JSONObject json = new JSONObject();
        json.put("list", page.getRecords());
        json.put("total", page.getTotal());
        return json;
    }
}

