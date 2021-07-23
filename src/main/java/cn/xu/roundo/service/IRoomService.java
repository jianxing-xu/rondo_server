package cn.xu.roundo.service;

import cn.xu.roundo.entity.Room;
import cn.xu.roundo.entity.vo.SearchVo;
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

}
