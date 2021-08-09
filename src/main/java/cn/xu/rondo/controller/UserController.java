package cn.xu.rondo.controller;


import cn.hutool.core.util.ReUtil;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.dto.user.LoginDTO;
import cn.xu.rondo.entity.dto.user.ResetPwdDTO;
import cn.xu.rondo.entity.dto.user.UpdatePwdDTO;
import cn.xu.rondo.entity.dto.user.UpdateUserDTO;
import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.enums.SwitchEnum;
import cn.xu.rondo.response.Response;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.socket.IMSocket;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.JWTUtils;
import cn.xu.rondo.utils.RedisUtil;
import cn.xu.rondo.utils.StringUtils;
import cn.xu.rondo.utils.params_resolver.UserId;
import cn.xu.rondo.utils.validation.EnumValue;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.yeauty.pojo.Session;

import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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

    @Autowired
    IMSocket imSocket;


    @PostMapping("/login")
    public JSONObject login(@RequestBody @Validated LoginDTO loginDTO) {
        final String account = loginDTO.getAccount();
        final String password = loginDTO.getPassword();
        final String plat = loginDTO.getPlat();
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
                user = userService.reByLogin(account, account.split("@")[0], plat);
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
            redis.deleteObject(Constants.mailCode(account));
            return new JSONObject() {{
                put("token", token);
            }};
        }
        throw new ApiException(EE.ACCOUNT_ERR);
    }

    @PostMapping("/admin/login")
    public JSONObject loginAdmin(@RequestBody @Validated LoginDTO loginDTO) {
        final String account = loginDTO.getAccount();
        final String password = loginDTO.getPassword();
        final String plat = loginDTO.getPlat();
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
                throw new ApiException(EE.FORBID);
            } else {
                HashMap<String, Object> data = new HashMap<>();
                data.put("user_id", user.getUser_id());
                data.put("user_account", user.getUser_account());
                String token = JWTUtils.createToken(data);
                // 返回token
                return new JSONObject() {{
                    put("token", token);
                }};
            }
        }
        throw new ApiException(EE.ACCOUNT_ERR);
    }

    /**
     * 获取登录用户信息
     *
     * @param userId 用户id
     * @return 用户数据
     */
    @PostMapping("/info")
    public JSONObject getMyUserInfo(@UserId Integer userId) {
        if (Common.isVisitor()) {
            return new JSONObject() {{
                put("user_id", -1);
                put("user_name", "Ghost");
                put("user_head", "/res/images/nohead.jpg");
                put("role", 0);
                put("myRoom", false);
            }};
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new ApiException(EE.ACCOUNT_EMPTY);
        }
        JSONObject mapUser = JSONObject.parseObject(JSON.toJSONString(user));

        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        wrapper.eq("room_user", user.getUser_id());
        Room room = roomService.getOne(wrapper);
        if ("123456".equals(user.getUser_password())) {
            mapUser.put("needNotice", 1);
        }
        mapUser.put("room", room);
        mapUser.remove("user_password");
        return mapUser;
    }

    /**
     * 获取其他用户信息
     *
     * @param userId 用户id
     * @return 用户消息
     */
    @GetMapping("/user_info/{userId}")
    public JSONObject getUserInfo(@PathVariable("userId") Integer userId) {
        User user = userService.getById(userId);
        JSONObject json = JSON.parseObject(JSON.toJSONString(user));
        Room room = roomService.getRoomByUser(userId);
        if (room != null) json.put("room", room);
        return json;
    }

    /**
     * 更新用户信息
     *
     * @param data   UpdateUserDTO
     * @param userId 用户id
     * @return 消息
     */
    @PostMapping("/update")
    public String updateInfo(@Validated @RequestBody UpdateUserDTO data,
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
        return "更新成功！";
    }

    @GetMapping("/tempToken")
    public String getTempToken() {
        return Constants.tempToken;
    }

    // 获取用户的各种操作排行 比如 点歌次数，顶歌次数，发送消息次数等等
    // @GetMapping("/rankList")

    /**
     * 修改密码
     *
     * @param userId 用户id
     * @return 是否成功
     */
    @PostMapping("/pwd/update")
    public String updatePwd(@RequestBody UpdatePwdDTO dto,
                            @UserId Integer userId) {
        final String newPwd = dto.getNewPwd();
        final String oldPwd = dto.getOldPwd();
        final boolean b = userService.updatePwd(newPwd, oldPwd, userId);
        return b ? "密码修改成功" : "密码修改失败";
    }

    /**
     * 邮箱验证 重置密码
     *
     * @return 消息
     */
    @PostMapping("/pwd/reset")
    public String resetPwd(@RequestBody ResetPwdDTO dto) {
        final String mail = dto.getMail();
        final String code = dto.getCode();
        final String newPwd = dto.getNewPwd();
        String cacheCode = redis.getCacheObject(Constants.mailCode(mail));
        if (!code.equals(cacheCode)) throw new ApiException(EE.MAIL_CODE_ERR);
        final boolean b = userService.updatePwd(newPwd, mail);
        redis.deleteObject(Constants.mailCode(mail));
        return b ? "重置成功，请使用新密码登录" : "重置密码失败";
    }

    //TODO: 禁言 OK!
    //TODO: 禁止点歌 OK!

    /**
     * 禁止或禁止点歌
     *
     * @param roomId 房间id
     * @param banId  需要禁止的用户
     * @param type   禁止类型0：禁言  1：禁止点歌
     * @param userId 操作用户id
     * @return 消息
     */
    @PostMapping("/shutdown/{type}/{roomId}/{banId}")
    public Response<String> shutdown(@PathVariable("roomId") Integer roomId,
                                     @PathVariable("banId") Integer banId,
                                     @PathVariable("type") @EnumValue(enumClass = SwitchEnum.class, message = "禁止类型不匹配") Integer type,
                                     @UserId Integer userId) {
        User user = userService.getById(userId);
        User banUser = userService.getById(banId);
        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        if (banUser == null) throw new ApiException(EE.ACCOUNT_EMPTY);
        if (!user.isAdmin() && userId.equals(room.getRoom_user())) throw new ApiException(EE.PERMISSION_LOW);
        if (userId.equals(banId)) throw new ApiException(EE.WHAT_FUCK);

        // 对管理员禁止 what fuck
        if (banUser.isAdmin()) throw new ApiException(EE.WHAT_FUCK);

        // cache('online_list_' . $room_id, null); // 暂时不知道作用

        userService.ban(roomId, banId, type);


        // 构造消息
        JSONObject data = new JSONObject();
        data.put("user", user);
        data.put("ban", banUser);
        data.put("msg", type == 0 ? "禁止发言" : "禁止点歌");
        data.put("type", type);
        String msg = new MsgVo(MsgVo.SHUT_DOWN, data).build();
        // 向房间发送消息
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);
        return Response.successTip("禁止操作成功！");
    }


    //TODO: 解除禁止OK!

    /**
     * 解除禁止或禁止点歌
     *
     * @param roomId 房间id
     * @param banId  需要解除禁止的用户
     * @param userId 操作用户id
     * @return 消息
     */
    @PostMapping("/removeBan/{roomId}/{banId}")
    public Response<String> removeBan(@PathVariable("roomId") Integer roomId,
                                      @PathVariable("banId") Integer banId,
                                      @UserId Integer userId) {
        User user = userService.getById(userId);
        User banUser = userService.getById(banId);
        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        if (banUser == null) throw new ApiException(EE.ACCOUNT_EMPTY);
        if (!user.isAdmin() && userId.equals(room.getRoom_user())) throw new ApiException(EE.PERMISSION_LOW);

        userService.removeBan(roomId, banId);

        // 构造消息
        JSONObject data = new JSONObject();
        data.put("user", user);
        data.put("ban", banUser);
        data.put("msg", "解除禁止状态");
        String msg = new MsgVo(MsgVo.REMOVE_BAN, data).build();
        // 向房间发送消息
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);
        return Response.successTip("解除禁止操作成功");
    }


    /**
     * 获取在线用户列表
     *
     * @param roomId 房间id
     * @param sync   'yes' 同步到数据库
     * @return 用户json列表
     */
    @GetMapping("/online/{roomId}/{sync}")
    public List<JSONObject> online(@PathVariable("roomId") Integer roomId,
                                   @PathVariable(value = "sync", required = false) String sync) {

        // 取到缓存就返回
        List<JSONObject> cacheList = redis.getCacheList(Constants.OnlineList + roomId);
        if (cacheList != null && cacheList.size() != 0) return cacheList;

        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        ConcurrentHashMap<String, Session> roomOnline = IMSocket.CHATMAP.get(String.valueOf(roomId));
        if (roomOnline == null) return new Vector<>();
        Set<String> ids = roomOnline.keySet().stream().map(String::valueOf).collect(Collectors.toSet());
        // 添加id为1的机器人
        ids.add("1");
        ids.add("206603");
        List<User> users = userService.getUsersByIds(ids);

        if ("yes".equals(sync)) {
            // 更新数据库中在线数据
            roomService.updateOnline(users.size(), roomId);
        }

        List<JSONObject> usersJson = new ArrayList<>();
        users.forEach(user -> {
            final JSONObject userJson = JSONObject.parseObject(JSON.toJSONString(user));
            userJson.put("user_admin", user.isAdmin());
            userJson.put("user_shutdown", userService.getCacheStatus(0, roomId, user.getUser_id()));
            userJson.put("user_songdown", userService.getCacheStatus(1, roomId, user.getUser_id()));
            userJson.remove("user_password");
            usersJson.add(userJson);
        });

        if (usersJson.size() != 0) {
            // 缓存在线列表5s
            redis.setCacheList(Constants.OnlineList + roomId, usersJson);
            redis.expire(Constants.OnlineList + roomId, 5, TimeUnit.SECONDS);
        }
        return usersJson;
    }


    //TODO: 三方登陆(qq,gitee,oschina,ding) Coming soon!!
    @Deprecated
    @PostMapping("/register")
    public User register(@RequestBody() Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        return null;
    }

    // TODO: coming soon
    @Deprecated
    @PostMapping("/openLogin")
    public User openLogin(@RequestBody() Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        return null;
    }
}

