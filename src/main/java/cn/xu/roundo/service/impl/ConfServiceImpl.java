package cn.xu.roundo.service.impl;

import cn.xu.roundo.entity.Conf;
import cn.xu.roundo.mapper.ConfMapper;
import cn.xu.roundo.service.IConfService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

}
