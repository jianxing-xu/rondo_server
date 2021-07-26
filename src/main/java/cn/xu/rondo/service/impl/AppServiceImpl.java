package cn.xu.rondo.service.impl;

import cn.xu.rondo.entity.App;
import cn.xu.rondo.mapper.AppMapper;
import cn.xu.rondo.service.IAppService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements IAppService {

}
