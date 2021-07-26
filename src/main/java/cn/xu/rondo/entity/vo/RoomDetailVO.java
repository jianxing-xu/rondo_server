package cn.xu.rondo.entity.vo;

import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.User;
import lombok.Data;

@Data
public class RoomDetailVO {
    private Room room;
    private User admin;
}
