package cn.xu.roundo.task;


import cn.xu.roundo.entity.Room;
import cn.xu.roundo.service.IRoomService;
import cn.xu.roundo.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@Configuration
public class SongTask {

    @Autowired
    IRoomService roomService;

    @Autowired
    RedisUtil redis;

    private static final Logger log = LoggerFactory.getLogger(SongTask.class);

    //    @Scheduled(fixedDelay = 2000)
    void execute() {
        List<Room> rooms = getAllRooms();
        if (rooms == null && rooms.size() == 0) {
            log.warn("暂无房间");
            return;
        }
        for (Room room : rooms) {

        }
    }

    // 获取所有房间
    public List<Room> getAllRooms() {
        List<Room> all_room = redis.getCacheList("all_room");
        if (all_room != null && all_room.size() != 0) {
            return all_room;
        }
        all_room = roomService.list();
        if (all_room == null) all_room = new ArrayList<>();
        redis.setCacheList("all_room", all_room);
        redis.expire("all_room", 5, TimeUnit.SECONDS);
        return all_room;
    }

}
