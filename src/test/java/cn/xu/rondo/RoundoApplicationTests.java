package cn.xu.rondo;

//import cn.xu.roundo.entity.Sa_conf;
//import cn.xu.roundo.service.ISa_confService;

import cn.hutool.core.date.*;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.GlobalThreadPool;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.utils.Common;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static java.lang.System.*;


@Slf4j
@SpringBootTest
class RoundoApplicationTests {
//    @Autowired
//    ISa_confService confService;

    @Test
    void contextLoads() {
    }


    @Test
    void testPagination() {
//        Page<Sa_conf> page = new Page<>(0, 10);
//        confService.page(page);
//        out.println("::::::::::::::::::::::DEBUG:::::::" + page.getTotal());
//        out.println(page.getRecords());
    }

    @Test
    public void getRegion() {
        String ip = "223.82.202.105";
        String body = HttpRequest.get("https://ipchaxun.com/" + ip + "/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.72 Safari/537.36")
                .timeout(5000)
                .execute()
                .body();

        if (body != null) {
            List<String> list = ReUtil.findAll("<span class=\"name\">归属地：<\\/span><span class=\"value\">(.*?)<\\/span>", body, 1);
            out.println(list);
        }
    }

    @Test
    public void testMakeDir() {
        File file = new File("/Users/jackson/Desktop/rondoo/upload");
        if (!file.exists()) {
            log.info("no exist!!!!!");
            file.mkdirs();
        }

    }

    @Test
    public void testHost() {
        final String hostIp = Common.getHostIp();
        final String hostName = Common.getHostName();
        log.info("hostIP: " + hostIp);
        log.info("hostName: " + hostName);
    }


    @Test
    public void testDate() {
        long now = System.currentTimeMillis();
        final Calendar start = CalendarUtil.calendar();
        start.set(Calendar.HOUR_OF_DAY, 18);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        final Calendar end = CalendarUtil.calendar();
        end.set(Calendar.HOUR_OF_DAY, 9);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);

        Date endDate = DateUtil.offset(DateUtil.date(end), DateField.DAY_OF_MONTH, 1);
        log.info(start.toString());
        log.info(endDate.toString());


        log.info("start" + start.getTimeInMillis());
        log.info("end" + endDate.getTime());
        log.info("now:" + now);
        log.info("distance: " + (endDate.getTime() - start.getTimeInMillis()));
        boolean in = now > start.getTimeInMillis() && now < endDate.getTime();
        log.info(in ? "在里面" : "不在里面");

    }

    @Test
    public void testThread() {

    }

    static public class RoomThread extends Thread {
        private Room room;

        public RoomThread(Room room) {
            setName(room.getRoom_id().toString());
            this.room = room;
        }

        @Override
        public void run() {
            while (true) {
                if (isInterrupted()) {
                    log.info("isInterrupted: " + room.getRoom_name() + "终止了！");
                    break;
                }
                log.info(room.getRoom_name() + "运行中....");

                if (!ThreadUtil.sleep(1000)) {
                    log.info(room.getRoom_name() + "终止了！");
                    break;
                }
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Map<String, Room> rooms = new HashMap<>();
        rooms.put("888", new Room() {{
            setRoom_id(888);
            setRoom_name("888房间");
        }});
        rooms.put("999", new Room() {{
            setRoom_id(888);
            setRoom_name("999房间");
        }});

        rooms.forEach((k, room) -> {
            Thread roomThread = new RoomThread(room);
            ThreadUtil.execute(roomThread);
        });

        ThreadUtil.sleep(1000);
        final Thread t99 = getThreadByName("999");
        ThreadUtil.sleep(5000);
        t99.interrupt();

    }

    public static Thread getThreadByName(String name) {
        for (Thread t : ThreadUtil.getThreads()) {
            log.info(t.getName() + "==========" + t.getId());
            if (t.getName().equals(name)) {
                System.out.println(t.getName());
                return t;
            }
        }
        return null;
    }


}
