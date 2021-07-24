package cn.xu.roundo.controller;


import cn.xu.roundo.entity.Room;
import cn.xu.roundo.entity.User;
import cn.xu.roundo.entity.dto.CreateRoom;
import cn.xu.roundo.entity.dto.UpdateRoomDTO;
import cn.xu.roundo.entity.vo.RoomDetailVO;
import cn.xu.roundo.entity.vo.SocketUrlVO;
import cn.xu.roundo.enums.ErrorEnum;
import cn.xu.roundo.response.exception.ApiException;
import cn.xu.roundo.service.IRoomService;
import cn.xu.roundo.service.IUserService;
import cn.xu.roundo.utils.*;
import cn.xu.roundo.utils.Common;
import cn.xu.roundo.utils.params_resolver.UserId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.xu.roundo.response.Response;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
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

    /**
     * 热门房间
     *
     * @return 响应对象
     */
    @GetMapping("/hot")
    public Response getHotRooms() {
        // 从redis中取，取到就返回
        List<Room> roomList = redis.getCacheList(Constants.roomList);
        if (roomList != null) {
            return new Response(roomList);
        }
        // 取不到就查询
        QueryWrapper<Room> wrap = new QueryWrapper<>();
        wrap.orderBy(true, false, "room_order");//排序值降序
        wrap.orderBy(true, false, "room_online");//在线人数降序
        wrap.orderByAsc("room_id");//id升序
        List<Room> list = roomService.list(wrap).subList(0, 10);
        // 将热门房间缓存到redis中
        redis.setCacheList(Constants.roomList, list);
        // 设置过期时间
        redis.expire(Constants.roomList, 3, TimeUnit.MINUTES);
        return new Response(list);
    }

    /**
     * 创建房间
     *
     * @param data   CreateRoom DTO
     * @param userId 用户id
     */
    @PostMapping("/create")
    public Response create(@RequestBody @Validated CreateRoom data, @UserId Integer userId) {
        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        wrapper.eq("room_user", userId);
        Room existRoom = roomService.getOne(wrapper);
        if (existRoom != null) {
            throw new ApiException(ErrorEnum.ROOM_EXIST);
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
        return new Response(null);
    }

    /**
     * 我的房间信息
     *
     * @param userId 用户id
     * @return 我的房间信息
     */
    @GetMapping("/myRoom")
    public Response myRoom(@UserId Integer userId) {
        QueryWrapper<Room> wrap = new QueryWrapper<>();
        wrap.eq("user_id", userId);
        Room room = roomService.getOne(wrap);
        room.setRoom_password(null);
        return new Response(room);
    }

    /**
     * 获取房间信息（加入房间）
     *
     * @param roomId       房间id
     * @param roomPassword 房间密码
     * @return 房间信息
     */
    @GetMapping("/getRoom")
    public Response getRoom(@RequestParam("room_id") @NotNull Integer roomId,
                            @RequestParam(value = "room_password", required = false) String roomPassword) {
        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(ErrorEnum.ROOM_NOT_FOUND);
        User user = userService.getById(room.getRoom_user());
        user.setUser_password(null);
        if (room.getRoom_status().equals(1))
            throw new ApiException(ErrorEnum.ROOM_BANED);
        RoomDetailVO vo = new RoomDetailVO();
        vo.setRoom(room);
        vo.setAdmin(user);
        if (Common.isVisitor()) {
            if (room.getRoom_public() == 1) throw new ApiException(ErrorEnum.VISITOR_BAN);
            return new Response(vo);
        }
        if (room.getRoom_public() == 1 && !room.getRoom_password().equals(roomPassword)) {
            throw new ApiException(ErrorEnum.ROOM_PSD_ERR);
        }
        return new Response(vo);
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
        if (room == null) throw new ApiException(ErrorEnum.INFO_QUERY_ERR);
        if (!userId.equals(room.getRoom_user()) && !user.isAdmin()) throw new ApiException(ErrorEnum.VISITOR_BAN);
        boolean reConnect = false;
        // 是公开房间就删除密码
        if (data.isPublic()) {
            data.setRoom_password("");
        } else if (data.getRoom_password() == null
                || data.getRoom_password().length() < 4
                || data.getRoom_password().length() > 16) {
            //加密房间：对密码进行校验
            throw new ApiException(ErrorEnum.PSD_VALID_ERR);
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
        //TODO：sendWebsocketMsg 发送通知到房间修改成功
        log.info(String.valueOf(reConnect));


        return roomService.updateById(room);
    }


    @CrossOrigin
    @GetMapping("/websocketUrl")
    public SocketUrlVO websocketUrl(@RequestParam(value = "channel") @NotBlank String channel,
                                    @RequestParam("password") @NotBlank String password,
                                    @UserId Integer userId,
                                    HttpServletRequest request) {
        String referer = request.getHeader("referer");
        String ip = Common.getIpAddr(request);
        Room room = roomService.getById(channel);
        User user = userService.getById(userId);
        if (room == null) throw new ApiException(ErrorEnum.INFO_QUERY_ERR);
        // 获取地区
        String region = redis.getCacheObject(Constants.ipAddress + ip);
        if (region == null) {
            region = roomService.getRegion(ip);
            if (StringUtils.isNotEmpty(region)) {
                redis.setCacheObject(Constants.ipAddress + ip, region);
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
            if (!room.isPublic()) throw new ApiException(ErrorEnum.VISITOR_BAN);
            vo.setAccount(ip);
        } else {
            if (!room.isPublic() && !password.equals(room.getRoom_password()))
                throw new ApiException(ErrorEnum.ROOM_PSD_ERR);
            vo.setAccount(String.valueOf(userId));
        }
        vo.setChannel(channel);
        //使用创建token的方式创建ticker
        HashMap<String, Object> data = new HashMap<>();
        data.put("account", vo.getAccount());
        data.put("channel", vo.getChannel());
        String ticker = JWTUtils.createTicker(data);
        vo.setTicker(ticker);

        // 最后一次登录，看是否给房间发送欢迎用户消息
        Boolean lastSend = redis.getCacheObject("channel_" + channel + "_user_" + ip);
        // 如果不存在就需要发送欢迎消息
        if (lastSend == null || !lastSend) {
            StringBuilder content = new StringBuilder("欢迎");
            if (StringUtils.isNotEmpty(region)) content.append("来自").append(region).append("的");
            if (!Common.isVisitor()) {
                content.append(user.getUser_name()).append("回来！");
            } else if (StringUtils.isNotEmpty(plat)) {
                content.append(plat).append("用户");
            } else {
                content.append("临时用户");
            }
            //TODO: sendWebsocketMsg 发送系统消息通知用户上线
            // 发送完消息，往redis里存一份记录，记录过期后才需要发送消息，三分钟内重新进入系统不会发送欢迎消息
            log.info(content.toString());
            redis.setCacheObject("channel_" + channel + "_user_" + ip, true);
            log.info("SEND_WEBSOCKET_MSG: 房间号:" + channel + "中的" + vo.getAccount() + "上线了！");
        }
        // 刷新是否欢迎标志
        redis.expire("channel_" + channel + "_user_" + ip, 3, TimeUnit.MINUTES);
        return vo;
    }
}

