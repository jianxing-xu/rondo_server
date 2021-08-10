package cn.xu.rondo.service.impl;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.mapper.UserMapper;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    RedisUtil redis;

    @Override
    public User reByLogin(String account, String name, String plat) {
        User user = new User();
        user.setUser_account(account);
        user.setUser_password("123456"); // set 中做加密(错误的做法)

        // 手动加密
        user.encodePwd();

        user.setUser_name(name);
        user.setUser_group(0);
        user.setUser_salt("Love");
        user.setUser_ipreg(Common.getIpAddr());
        log.info(String.valueOf(System.currentTimeMillis()));
        user.setUser_createtime((int) (System.currentTimeMillis() / 1000));
        user.setUser_updatetime((int) (System.currentTimeMillis() / 1000));
        user.setUser_touchtip("大帅比");
        user.setRole(0);
        user.setUser_device(plat);
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
        wrapper.eq("user_account", account);
        User user = userMapper.selectOne(wrapper);
        // 验证加密密码正确性
        if (user != null && user.verifyPwd(password)) {
            return user;
        }
        return null;
    }

    @Override
    public boolean updatePwd(String newPwd, String oldPwd, Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(EE.ACCOUNT_EMPTY);
        if (!user.verifyPwd(oldPwd)) throw new ApiException(EE.PWD_ERROR);
        user.setUser_password(newPwd);
        user.encodePwd();
        userMapper.updateById(user);
        return true;
    }

    @Override
    public boolean updatePwd(String newPwd, String mail) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account", mail);
        User user = userMapper.selectOne(wrapper);
        if (user == null) throw new ApiException(EE.ACCOUNT_EMPTY);
        user.setUser_password(newPwd);
        user.encodePwd();
        userMapper.updateById(user);
        return true;
    }

    @Override
    public void removeBan(Integer roomId, Integer banId) {
        Set<Integer> shutdownSet = redis.getCacheSet(Constants.Shutdown + roomId);
        Set<Integer> songDownSet = redis.getCacheSet(Constants.SongDown + roomId);
        shutdownSet.remove(banId);
        songDownSet.remove(banId);
        redis.setCacheSetForDel(Constants.Shutdown + roomId, shutdownSet);
        redis.setCacheSetForDel(Constants.SongDown + roomId, songDownSet);
    }

    @Override
    public void ban(Integer roomId, Integer banId, Integer type) {
        if (type == 0) {
            // 添加禁言
            Set<Integer> shutdownSet = redis.getCacheSet(Constants.Shutdown + roomId);
            if (shutdownSet == null) shutdownSet = new HashSet<>();
            shutdownSet.add(banId);
            redis.setCacheSetForDel(Constants.Shutdown + roomId, shutdownSet);
        } else {
            // 添加禁止点歌
            Set<Integer> songDownSet = redis.getCacheSet(Constants.SongDown + roomId);
            if (songDownSet == null) songDownSet = new HashSet<>();
            songDownSet.add(banId);
            redis.setCacheSetForDel(Constants.SongDown + roomId, songDownSet);
        }
    }

    @Override
    public List<User> getUsersByIds(Set<String> ids) {
        Set<Integer> collect = new HashSet<>();
        try {
            collect = ids.stream().filter((item) -> !Common.isIpv4(item)).map(Integer::parseInt).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error(e.toString());
        }
        QueryWrapper<User> wrap = new QueryWrapper<>();
        wrap.in("user_id", collect);
        wrap.orderByAsc("user_id");
        return userMapper.selectList(wrap);
    }


    /**
     * 获取用户是否禁言或禁止点歌
     *
     * @param type   0：获取禁言状态  1：获取禁止点歌状态
     * @param roomId 房间id
     * @param userId 用户id
     * @return 是否
     */
    public boolean getCacheStatus(int type, Integer roomId, Integer userId) {
        switch (type) {
            case 0:
                Set<Integer> chatDownSet = redis.getCacheSet(Constants.Shutdown + roomId);
                if (chatDownSet == null) return false;
                return chatDownSet.contains(userId);
            case 1:
                Set<Integer> songDownSet = redis.getCacheSet(Constants.SongDown + roomId);
                if (songDownSet == null) return false;
                return songDownSet.contains(userId);
            default:
                return false;
        }
    }
}
