package cn.xu.rondo.service.impl;

import cn.xu.rondo.entity.Conf;
import cn.xu.rondo.mapper.ConfMapper;
import cn.xu.rondo.service.IConfService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 配置表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class ConfServiceImpl extends ServiceImpl<ConfMapper, Conf> implements IConfService {

    @Override
    // 获取配置值
    public String get(String key) {
        QueryWrapper<Conf> wrap = new QueryWrapper<>();
        wrap.likeLeft("conf_key", key);
        final Conf conf = getOne(wrap);
        if(conf == null) return "";
        return conf.getConf_value();
    }
}
