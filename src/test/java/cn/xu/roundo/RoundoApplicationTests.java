package cn.xu.roundo;

//import cn.xu.roundo.entity.Sa_conf;
//import cn.xu.roundo.service.ISa_confService;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static java.lang.System.*;

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


}
