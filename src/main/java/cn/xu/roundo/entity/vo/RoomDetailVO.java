package cn.xu.roundo.entity.vo;

import cn.xu.roundo.entity.Room;
import cn.xu.roundo.entity.User;
import lombok.Data;

@Data
public class RoomDetailVO {
    private Room room;
    private User admin;
}
