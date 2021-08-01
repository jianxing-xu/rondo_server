package cn.xu.rondo.service;

import cn.xu.rondo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface IUserService extends IService<User> {
    User reByLogin(String account, String name, String plat);

    User login(String account, String password);

    boolean updatePwd(String newPwd, String oldPwd, Integer userId);

    boolean updatePwd(String newPwd, String mail);

    void removeBan(Integer roomId, Integer banId);

    void ban(Integer roomId, Integer banId, Integer type);

    List<User> getUsersByIds(Set<String> ids);

    boolean getCacheStatus(int type, Integer roomId, Integer userId);
}
