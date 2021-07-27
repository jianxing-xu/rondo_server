package cn.xu.rondo;

//import cn.xu.roundo.entity.Sa_conf;
//import cn.xu.roundo.service.ISa_confService;

import cn.hutool.core.date.*;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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


}
