package cn.xu.roundo.service;

import cn.xu.roundo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface IUserService extends IService<User> {
    User reByLogin(String account, String name);

    User login(String account, String password);
}
