package cn.xu.rondo.service;

import cn.xu.rondo.entity.Room;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 房间表 服务类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface IRoomService extends IService<Room> {
    String getRegion(String ip);

    String getPlatForReferer(String referer);

    Room getRoomByUser(Integer userId);

    void updateOnline(int size, Integer roomId);
}
