package cn.xu.roundo.controller;


import cn.hutool.core.util.ReUtil;
import cn.xu.roundo.entity.Room;
import cn.xu.roundo.entity.User;
import cn.xu.roundo.entity.dto.UpdateUserDTO;
import cn.xu.roundo.enums.ErrorEnum;
import cn.xu.roundo.response.exception.ApiException;
import cn.xu.roundo.service.IRoomService;
import cn.xu.roundo.service.IUserService;
import cn.xu.roundo.utils.Common;
import cn.xu.roundo.utils.Constants;
import cn.xu.roundo.utils.JWTUtils;
import cn.xu.roundo.utils.RedisUtil;
import cn.xu.roundo.utils.StringUtils;
import cn.xu.roundo.utils.params_resolver.UserId;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.xu.roundo.response.Response;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Validated
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    IUserService userService;

    @Autowired
    IRoomService roomService;

    @Autowired
    RedisUtil redis;


    @PostMapping("/login")
    public Response login(@NotBlank @RequestParam("account") String account,
                          @NotBlank @RequestParam("password") String password,
                          @RequestParam("plat") String plat) {
        String code = "" + redis.getCacheObject(Constants.mailCode(account)); // TODO: 此处从redis中获取邮箱验证码
        // 邮箱登录
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        User user;
        // 如果提交的密码和验证码一致，表示用邮箱验证登录
        if (password.equals(code)) {
            //  全数字代表用user_id登录
            if (ReUtil.isMatch("^[1-9][0-9]*$", account)) {
                queryWrapper.eq("user_id", account);
            } else {
                //否则就是账号登录
                queryWrapper.eq("user_account", account);
            }
            user = userService.getOne(queryWrapper);
            // 没有找到用户
            if (user == null) {
                // 注册并登录，账户为邮箱，昵称为@前面的数字
                user = userService.reByLogin(account, account.split("@")[0]);
                // TODO: bbbug中 cache 了 MAIL_{account}
            }
        } else {
            // 账户account登录
            user = userService.login(account, password);
        }
        if (user != null) {
            // 创建jwt token
            HashMap<String, Object> map = new HashMap<>();
            map.put("user_id", user.getUser_id());
            map.put("user_account", account);
            String token = JWTUtils.createToken(map);
            // 返回token
            return new Response(new HashMap<String, String>() {{
                put("token", token);
            }});
        }
        throw new ApiException(ErrorEnum.ACCOUNT_ERR);
    }

    @PostMapping("/admin/login")
    public Response loginAdmin(@NotBlank @RequestParam("account") String account,
                               @NotBlank @RequestParam("password") String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (ReUtil.isMatch("^[1-9][0-9]*$", account)) {
            queryWrapper.eq("user_id", account);
        } else {
            //否则就是账号登录
            queryWrapper.eq("user_account", account);
        }
        User user = userService.getOne(queryWrapper);
        if (user != null && password.equals(user.getUser_password())) {
            Integer role = user.getRole();
            if (role != 1) {
                throw new ApiException(ErrorEnum.FORBID);
            } else {
                HashMap<String, Object> data = new HashMap<>();
                data.put("user_id", user.getUser_id());
                data.put("user_account", user.getUser_account());
                String token = JWTUtils.createToken(data);
                // 返回token
                return new Response(new HashMap<String, String>() {{
                    put("token", token);
                }});
            }
        }
        throw new ApiException(ErrorEnum.ACCOUNT_ERR);
    }

    /**
     * 获取登录用户信息
     *
     * @param userId
     * @return
     */
    @PostMapping("/info")
    public Response getMyUserInfo(@UserId Integer userId) {
        if (Common.isVisitor()) {
            return new Response(new HashMap<String, Object>() {{
                put("user_id", -1);
                put("user_name", "Ghost");
                put("user_head", "new/images/nohead.jpg");
                put("role", 0);
                put("myRoom", false);
            }});
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new ApiException(ErrorEnum.ACCOUNT_EMPTY);
        }
        Map mapUser = JSON.parseObject(JSON.toJSONString(user), Map.class);

        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("room_user", user.getUser_id());
        Room room = roomService.getOne(wrapper);
        if ("123456".equals(user.getUser_password())) {
            mapUser.put("needNotice", 1);
        }
        mapUser.put("room", room);
        mapUser.remove("user_password");
        return new Response(mapUser);
    }

    /**
     * 获取其他用户信息
     *
     * @param userId 用户id
     * @return
     */
    @GetMapping("/user_info/{userId}")
    public Response getUserInfo(@PathVariable("userId") String userId) {
        User user = userService.getById(userId);
        user.setUser_password(null);
        return new Response(user);
    }

    /**
     * 更新用户信息
     *
     * @param data   UpdateUserDTO
     * @param userId
     * @return
     */
    @PostMapping("/update")
    public Response updateInfo(@Validated @RequestBody UpdateUserDTO data,
                               @UserId Integer userId) {
        User user = new User();
        user.setUser_id(userId);
        user.setUser_head(data.getUserHead());
        user.setUser_sex(data.getUserSex());
        user.setUser_touchtip(data.getUserTouchTip());
        user.setUser_name(data.getUserName());
        user.setUser_remark(data.getUserRemark());
        if (!StringUtils.isEmpty(data.getUserPassword())) {
            user.setUser_password(data.getUserPassword());
        }
        userService.updateById(user);
        return new Response(null);
    }

    @GetMapping("/tempToken")
    public String getTempToken() {
        return Constants.tempToken;
    }


    //TODO: 禁言

    //TODO: 禁止点歌

    //TODO: 解除禁止

    //TODO: 摸一摸

    //TODO: 三方登陆(qq,gitee,oschina,ding)

    @Deprecated
    @PostMapping("/register")
    public User register(@RequestBody() Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        return null;
    }
}

