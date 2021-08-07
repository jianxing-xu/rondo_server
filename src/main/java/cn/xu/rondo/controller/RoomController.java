package cn.xu.rondo.controller;


import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.dto.CreateRoom;
import cn.xu.rondo.entity.dto.UpdateRoomDTO;
import cn.xu.rondo.entity.vo.HotRoomVO;
import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.entity.vo.RoomDetailVO;
import cn.xu.rondo.entity.vo.SocketUrlVO;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.socket.IMSocket;
import cn.xu.rondo.socket.RoomThread;
import cn.xu.rondo.task.SongTask;
import cn.xu.rondo.utils.*;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.params_resolver.UserId;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.xu.rondo.response.Response;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 房间表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Validated
@RestController
@RequestMapping("/room")
public class RoomController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    IRoomService roomService;

    @Autowired
    IUserService userService;

    @Autowired
    RedisUtil redis;

    @Autowired
    IMSocket imSocket;

    /**
     * 热门房间
     *
     * @return 响应对象
     */
    @GetMapping("/hot")
    public List<HotRoomVO> getHotRooms() {
        // 从redis中取，取到就返回
        List<HotRoomVO> roomList = redis.getCacheList(Constants.roomList);
        if (roomList != null && roomList.size() != 0) return roomList;
        // 取不到就查询
        final List<HotRoomVO> list = roomService.hotRooms();
        // 将热门房间缓存到redis中
        redis.setCacheList(Constants.roomList, list);
        // 设置过期时间
        redis.expire(Constants.roomList, 3, TimeUnit.MINUTES);
        return list;
    }

    /**
     * 创建房间
     *
     * @param data   CreateRoom DTO
     * @param userId 用户id
     */
    @PostMapping("/create")
    public String create(@RequestBody @Validated CreateRoom data, @UserId Integer userId) {
        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        wrapper.eq("room_user", userId);
        Room existRoom = roomService.getOne(wrapper);
        if (existRoom != null) {
            throw new ApiException(EE.ROOM_EXIST);
        }
        Room newRoom = new Room();
        newRoom.setRoom_user(userId);
        newRoom.setRoom_name(data.getRoom_name());
        newRoom.setRoom_notice(data.getRoom_notice());
        newRoom.setRoom_type(data.getRoom_type());
        newRoom.setRoom_public(data.getRoom_public());
        newRoom.setRoom_votepass(data.getRoom_votepass());
        newRoom.setRoom_votepercent(data.getRoom_votepercent());
        newRoom.setRoom_addsong(data.getRoom_addsong());
        newRoom.setRoom_sendmsg(data.getRoom_sendmsg());
        newRoom.setRoom_robot(data.getRoom_robot());
        roomService.save(newRoom);
        return "创建成功！";
    }

    /**
     * 我的房间信息
     *
     * @param userId 用户id
     * @return 我的房间信息
     */
    @GetMapping("/myRoom")
    public Room myRoom(@UserId Integer userId) {
        QueryWrapper<Room> wrap = new QueryWrapper<>();
        wrap.eq("user_id", userId);
        Room room = roomService.getOne(wrap);
        room.setRoom_password(null);
        return room;
    }

    /**
     * 获取房间信息（加入房间）
     *
     * @param roomId       房间id
     * @param roomPassword 房间密码
     * @return 房间信息
     */
    @GetMapping("/info/{room_id}")
    public RoomDetailVO info(@PathVariable("room_id") @NotNull Integer roomId,
                             @RequestParam(value = "room_password", required = false) String roomPassword,
                             @UserId Integer userId) {
        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        User admin = userService.getById(room.getRoom_user());
        if (room.getRoom_status().equals(1))
            throw new ApiException(EE.ROOM_BANED);

        RoomDetailVO vo = new RoomDetailVO();
        vo.setRoom(room);
        vo.setAdmin(admin);
        if (Common.isVisitor()) {
            if (!room.isPublic()) throw new ApiException(EE.VISITOR_BAN);
            return vo;
        }
        User user = userService.getById(userId);
        if (user == null) throw new ApiException(EE.INFO_QUERY_ERR);
        // 加密房间 && 不是房主 && 不是管理员就需要再次判断
        if (!room.isPublic() && !room.getRoom_user().equals(userId) && !user.isAdmin()) {
            // 取用户输入过的密码缓存
            String savedPassword = redis.getCacheObject(Constants.SavedPwd(roomId, userId));
            // 如果缓存存在
            if (savedPassword != null) {
                //并且匹配房间密码则返回房间信息
                if (savedPassword.equals(room.getRoom_password())) {
                    redis.setCacheObject(Constants.SavedPwd(roomId, userId), room.getRoom_password());
                    redis.expire(Constants.SavedPwd(roomId, userId), 1, TimeUnit.DAYS);
                    return vo;
                } else {
                    // 有缓存但缓存和房间密码不匹配，表示在用户输入密码后，房间修改了密码
                    redis.deleteObject(Constants.SavedPwd(roomId, userId));
                    throw new ApiException(EE.NEED_PWD);
                }
            }
            // 没有密码缓存  并且输入了密码字段
            if (roomPassword != null && StringUtils.isNotEmpty(roomPassword)) {
                // 密码正确
                if (roomPassword.equals(room.getRoom_password())) {
                    redis.setCacheObject(Constants.SavedPwd(roomId, userId), room.getRoom_password());
                    redis.expire(Constants.SavedPwd(roomId, userId), 1, TimeUnit.DAYS);
                    return vo;
                } else {
                    throw new ApiException(EE.ROOM_PWD_ERROR);
                }
            }
            // 没输入密码字段
            throw new ApiException(EE.POP_INPUT_PWD);
        }
        return vo;
    }

    /**
     * 更新房间信息
     *
     * @param data   UpdateRoomDTO
     * @param userId 用户id
     * @return 消息
     */
    @PostMapping("/update")
    public boolean update(@RequestBody @Validated UpdateRoomDTO data, @UserId Integer userId) {
        Integer room_id = data.getRoom_id();
        Room room = roomService.getById(room_id);
        User user = userService.getById(userId);
        if (room == null) throw new ApiException(EE.INFO_QUERY_ERR);
        if (!userId.equals(room.getRoom_user()) && !user.isAdmin()) throw new ApiException(EE.VISITOR_BAN);
        boolean reConnect;
        // 是公开房间就删除密码
        if (data.isPublic()) {
            data.setRoom_password("");
        } else if (data.getRoom_password() == null
                || data.getRoom_password().length() < 4
                || data.getRoom_password().length() > 16) {
            //加密房间：对密码进行校验
            throw new ApiException(EE.PSD_VALID_ERR);
        }
        // 如果修改了房间加密信息，就需要重新加载（发送websocket消息到前台）
        reConnect = room.isPublic() != data.isPublic();
        room.setRoom_name(data.getRoom_name());
        room.setRoom_notice(data.getRoom_notice());
        room.setRoom_type(data.getRoom_type());
        room.setRoom_public(data.getRoom_public());
        room.setRoom_password(data.getRoom_password());
        room.setRoom_votepass(data.getRoom_votepass());
        room.setRoom_votepercent(data.getRoom_votepercent());
        room.setRoom_addsong(data.getRoom_addsong());
        room.setRoom_sendmsg(data.getRoom_sendmsg());
        room.setRoom_robot(data.getRoom_robot());
        room.setRoom_pushsongcd(data.getRoom_pushsongcd());
        room.setRoom_pushdaycount(data.getRoom_pushdaycount());
        room.setRoom_addcount(data.getRoom_addcount());
        room.setRoom_addsongcd(data.getRoom_addsongcd());
        room.setRoom_background(data.getRoom_background());
        room.setRoom_id(room_id);
        //TODO：sendWebsocketMsg 发送通知到房间修改成功 OK!
        log.info(String.valueOf(reConnect));

        JSONObject msgData = new JSONObject();
        msgData.put("reConnect", reConnect);
        msgData.put("content", reConnect ? "房间密码已修改" : "房间信息已修改");
        msgData.put("user", user);
        String msg = new MsgVo(MsgVo.ROOM_UPDATE, msgData).build();
        imSocket.sendMsgToRoom(String.valueOf(room_id), msg);
        return roomService.updateById(room);
    }


    @CrossOrigin
    @GetMapping("/websocketUrl")
    public SocketUrlVO websocketUrl(@RequestParam(value = "channel") @NotBlank String channel,
                                    @RequestParam(value = "password", required = false) String password,
                                    @UserId Integer userId,
                                    HttpServletRequest request) {
        String referer = request.getHeader("referer");
        String ip = Common.getIpAddr();
        Room room = roomService.getById(channel);
        User user = userService.getById(userId);
        if (room == null) throw new ApiException(EE.INFO_QUERY_ERR);
        // 获取地区
        String region = redis.getCacheObject(Constants.ipAddress + ip);
        if (region == null) {
            region = roomService.getRegion(ip);
            if (StringUtils.isNotEmpty(region)) {
                redis.setCacheObject(Constants.ipAddress + ip, region);
                redis.expire(Constants.ipAddress + ip, 3600, TimeUnit.SECONDS);
            }
        }
        // 获取平台
        String plat = "";
        if (referer != null) {
            plat = roomService.getPlatForReferer(referer);
        }

        SocketUrlVO vo = new SocketUrlVO();
        if (Common.isVisitor()) {
            // 游客不允许访问加密房间
            if (!room.isPublic()) throw new ApiException(EE.VISITOR_BAN);
            vo.setAccount(ip);
        } else {
            if (!user.isAdmin() && !userId.equals(room.getRoom_user()) && !room.isPublic()) {
                String savedPassword = redis.getCacheObject(Constants.SavedPwd(room.getRoom_id(), userId));
                if (!savedPassword.equals(room.getRoom_password()) && !room.getRoom_password().equals(password)) {
                    throw new ApiException(EE.ROOM_PSD_ERR);
                }
            }
            vo.setAccount(String.valueOf(userId));
        }
        vo.setChannel(channel);
        //使用创建token的方式创建ticker
        HashMap<String, Object> data = new HashMap<>();
        data.put("account", vo.getAccount());
        data.put("channel", vo.getChannel());
        String ticket = JWTUtils.createTicker(data);
        vo.setTicket(ticket);

        // 最后一次登录，看是否给房间发送欢迎用户消息
        Boolean lastSend = redis.getCacheObject("channel_" + channel + "_user_" + ip);
        // 如果不存在就需要发送欢迎消息
        if (lastSend == null || !lastSend) {
            StringBuilder content = new StringBuilder("欢迎");
            if (StringUtils.isNotEmpty(region)) content.append("来自").append(region).append("的");
            int userType = 0;
            if (!Common.isVisitor()) {
//                content.append(user.getUser_name()).append("回来！");
            } else if (StringUtils.isNotEmpty(plat)) {
//                content.append(plat).append("用户");
                userType = 1;
            } else {
                userType = 2;
//                content.append("临时用户");
            }
            //TODO: sendWebsocketMsg 发送系统消息通知用户上线OK!
            // 发送完消息，往redis里存一份记录，记录过期后才需要发送消息，三分钟内重新进入系统不会发送欢迎消息OK!
            log.info(content.toString());
            redis.setCacheObject("channel_" + channel + "_user_" + ip, true);
            log.info("SEND_WEBSOCKET_MSG: 房间号:" + channel + "中的" + vo.getAccount() + "上线了！");
//            if (!user.isAdmin()) {
            JSONObject jsonData = new JSONObject();
            jsonData.put("userType", userType);
            jsonData.put("name", user.getUser_name());
            jsonData.put("where", region);
            jsonData.put("plat", plat);
            jsonData.put("user", user); //TODO: user.setUser_password(null);
            jsonData.put("content", content);
            String msg = new MsgVo(MsgVo.JOIN, jsonData).build();
            imSocket.sendMsgToRoom(channel, msg);
//            }
        }
        // 刷新是否欢迎标志
        redis.expire("channel_" + channel + "_user_" + ip, 3, TimeUnit.MINUTES);
        return vo;
    }


    //===================================================
    @DeleteMapping("/del/{room_id}")
    public String delRoom(@PathVariable Integer room_id, @UserId Integer userId) {
        if (!checkUser(userId).isAdmin()) throw ERR(EE.PERMISSION_LOW);
        checkRoom(room_id);
        if (roomService.removeById(room_id)) {
            // 删除房间数据
            SongTask.rooms.remove(room_id);
            // 找到房间线程并停止
            final RoomThread roomThread = (RoomThread) Common.getThreadByName(Constants.RoomThreadPREFIX + room_id);
            if (roomThread != null) {
                //roomThread.interrupt(); // 调用 interrupt 停止线程 可能会导致redis异常等
                roomThread.exit(); // 设置退出循环标志位停止线程 更安全一点 会把run执行完全
            }
        }
        return "删除成功!";
    }

}

