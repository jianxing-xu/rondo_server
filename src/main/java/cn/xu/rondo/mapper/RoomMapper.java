package cn.xu.rondo.mapper;

import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.vo.HotRoomVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 房间表 Mapper 接口
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface RoomMapper extends BaseMapper<Room> {
    @Select("SELECT \n" +
            "room_addcount,room_addsong,room_addsongcd,room_app,room_background,room_createtime,\n" +
            "room_fullpage,room_hide,\n" +
            "room_id,room_name,room_notice,\n" +
            "room_online,\n" +
            "room_order,\n" +
            "room_playone,room_public,room_pushdaycount,room_pushsongcd,room_realonline,\n" +
            "room_reason,room_robot,room_sendmsg,room_status,room_type,room_updatetime,\n" +
            "room_user,room_votepass,room_votepercent,user_group,user_head,user_id,user_name\n" +
            "FROM\n" +
            "sa_room t1\n" +
            "LEFT JOIN sa_user ON room_user = user_id ORDER BY room_order desc,room_online desc,room_id ASC")
    List<HotRoomVO> hotRooms();
}
