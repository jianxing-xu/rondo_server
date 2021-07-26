package cn.xu.rondo.service.impl;

import cn.xu.rondo.entity.Access;
import cn.xu.rondo.mapper.AccessMapper;
import cn.xu.rondo.service.IAccessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 授权信息表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class AccessServiceImpl extends ServiceImpl<AccessMapper, Access> implements IAccessService {

}
