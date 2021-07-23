package cn.xu.roundo.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.ReUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpUtil;
import cn.xu.roundo.entity.User;
import cn.xu.roundo.mapper.UserMapper;
import cn.xu.roundo.service.IUserService;
import cn.xu.roundo.utils.Common;
import cn.xu.roundo.utils.ServletUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserMapper userMapper;

    @Override
    public User reByLogin(String account, String name) {
        User user = new User();
        user.setUser_account(account);
        user.setUser_password("123456");
        user.setUser_name(name);
        user.setUser_group(0);
        user.setUser_salt("Love");
        user.setUser_ipreg(Common.getIpAddr(ServletUtils.getRequest()));
        log.info(String.valueOf(System.currentTimeMillis()));
        user.setUser_createtime((int) (System.currentTimeMillis() / 1000));
        user.setUser_updatetime((int) (System.currentTimeMillis() / 1000));
        user.setUser_touchtip("大帅比");
        user.setRole(0);
        try {
            String content = HttpUtil.get("http://guozhivip.com/yy/api/api.php");
            String mark = ReUtil.get("\"(.*?)\"", content, 1);
            log.info(mark);
            user.setUser_remark(mark);
        } catch (Exception e) {
            user.setUser_remark("其他人都有，就我没有！");
        }
        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String account, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account", account).eq("user_password", password);
        User user = userMapper.selectOne(wrapper);
        return user;
    }
}
