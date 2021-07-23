package cn.xu.roundo.service.impl;

import cn.xu.roundo.entity.App;
import cn.xu.roundo.mapper.AppMapper;
import cn.xu.roundo.service.IAppService;
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
