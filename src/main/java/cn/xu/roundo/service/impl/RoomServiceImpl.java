package cn.xu.roundo.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.xu.roundo.entity.Room;
import cn.xu.roundo.mapper.RoomMapper;
import cn.xu.roundo.service.IRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.System.out;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Override
    public String getRegion(String ip) {
        try {
            String body = HttpRequest.get("https://ipchaxun.com/" + ip + "/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.72 Safari/537.36")
                    .timeout(5000)
                    .execute()
                    .body();
            if (body != null) {
                List<String> list = ReUtil.findAll("<span class=\"name\">归属地：<\\/span><span class=\"value\">(.*?)<\\/span>", body, 1);
                if (list.isEmpty()) {
                    return "";
                }
                return list.get(0);
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String getPlatForReferer(String referer) {
        String plat = "";
        if (referer.contains("v2ex.com")) {
            plat = "V2EX";
        } else if (referer.contains("juejin.cn")) {
            plat = "掘金";
        } else if (referer.contains("oschina.net")) {
            plat = "开源中国";
        } else if (referer.contains("gitee.com")) {
            plat = "Gitee";
        } else if (referer.contains("segmentfault.com")) {
            plat = "思否";
        } else if (referer.contains("jianshu.com")) {
            plat = "简书";
        } else if (referer.contains("csdn.net")) {
            plat = "CSDN";
        } else if (referer.contains("github.com")) {
            plat = "Github";
        }
        return plat;
    }


}
