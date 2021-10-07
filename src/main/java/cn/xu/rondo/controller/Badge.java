package cn.xu.rondo.controller;

import cn.hutool.core.codec.Base64;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.vo.SongQueueVo;
import cn.xu.rondo.interceptor.VisitorInter;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/badge")
public class Badge {
    @Autowired
    private RedisUtil redis;
    @Autowired
    private IRoomService roomService;
    @Value("${server.port}")
    private String port;

    /**
     * 返回正在播放的标签svg图片
     *
     * @param room_id  房间id
     * @param response 响应对象
     * @throws IOException 异常
     */
    @VisitorInter
    @GetMapping("/badge/{room_id}")
    public void badge(@PathVariable Integer room_id, HttpServletResponse response) throws IOException {
        SongQueueVo now = redis.getCacheObject(Constants.SongNow + room_id);
        Room room = roomService.getById(room_id);
        final ServletOutputStream outputStream = response.getOutputStream();
        if (now == null || room == null) {
            outputStream.write("no data".getBytes());
            outputStream.flush();
            outputStream.close();
            return;
        }
        if (now.getSong() == null) return;
        response.setHeader("Content-Type", "image/svg+xml");
        String name = now.getSong().getName();
        String singer = now.getSong().getSinger();
        String pic = "data:image/jpeg;base64," + Base64.encode(Common.readFileByUrl(now.getSong().getPic()));
        String userName = now.getUser().getUser_name();
        String bg = "data:image/jpeg;base64," + Base64.encode(Common.readFileByUrl("http://127.0.0.1:" + port + "/api/res/images/player_bg.png"));
        String bar = "data:image/jpeg;base64," + Base64.encode(Common.readFileByUrl("http://127.0.0.1:" + port + "/api/res/images/player_bar.png"));

        String roomName = room.getRoom_name();
        String str = "\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"400\" height=\"160\">\n" +
                "<defs>\n" +
                "<filter id=\"filter_image\" x=\"0\" y=\"0\">\n" +
                "  <feGaussianBlur stdDeviation=\"10\" />\n" +
                "</filter>\n" +
                "</defs>\n" +
                "    <rect x=\"0\" y=\"0\" width=\"400\" height=\"140\" rx=\"10\" ry=\"10\" style=\"fill:#333;stroke-width:5;\" />\n" +
                "    <rect id=\"songImageRect\" x=\"30\" y=\"30\" width=\"80\" height=\"80\" rx=\"100\" ry=\"100\" style=\"fill:#333333;stroke-width:5;\" />\n" +
                "    <clipPath id=\"songBgPath\">\n" +
                "        <use xlink:href=\"#songBgRech\" />\n" +
                "    </clipPath>\n" +
                "    <clipPath id=\"songImagePath\">\n" +
                "        <use xlink:href=\"#songImageRect\" />\n" +
                "    </clipPath>\n" +
                "    \n" +
                "    <g font-family=\"Consolas, PingFangSC-Regular, Microsoft YaHei\" font-size=\"20\">\n" +
                "    <text x=\"137\" y=\"41\" fill=\"black\">" + name + "</text>\n" +
                "    <text x=\"138\" y=\"40\" fill=\"white\">" + name + "</text>\n" +
                "    </g>\n" +
                "    \n" +
                "    <g font-family=\"Consolas, PingFangSC-Regular, Microsoft YaHei\" font-size=\"14\">\n" +
                "    <text size=\"16\" x=\"137\" y=\"59\" fill=\"#000\">" + singer + "</text>\n" +
                "    <text size=\"16\" x=\"138\" y=\"60\" fill=\"#999\">" + singer + "</text>\n" +
                "    </g>\n" +
                "    \n" +
                "    <g font-family=\"Consolas, PingFangSC-Regular, Microsoft YaHei\" font-size=\"12\">\n" +
                "    <text size=\"16\" x=\"137\" y=\"99\" fill=\"#000\">" + userName + "</text>\n" +
                "    <text size=\"16\" x=\"138\" y=\"100\" fill=\"#999\">" + userName + "</text>\n" +
                "    </g>\n" +
                "    \n" +
                "    \n" +
                "    <a xlink:href=\"https://rondo.com/{$room['room_id']}\" target=\"_blank\" style=\"cursor:pointer;\">\n" +
                "        <g font-family=\"Consolas, PingFangSC-Regular, Microsoft YaHei\" font-size=\"12\" text-anchor=\"right\">\n" +
                "            <text size=\"16\" x=\"137\" y=\"119\" fill=\"#000\">ID:" + room_id + "  " + roomName + "</text>\n" +
                "            <text size=\"16\" x=\"138\" y=\"120\" fill=\"#666\">ID:" + room_id + "  " + roomName + "</text>\n" +
                "        </g>\n" +
                "    </a>\n" +
                "    <a xlink:href=\"https://rondo.com/\" target=\"_blank\" style=\"cursor:pointer;\">\n" +
                "        <g font-family=\"Consolas, PingFangSC-Regular, Microsoft YaHei\" font-size=\"12\" text-anchor=\"right\">\n" +
                "            <text size=\"16\" x=\"320\" y=\"158\" fill=\"#aaa\" >RONDO.COM</text>\n" +
                "        </g>\n" +
                "    </a>\n" +
                "    <image xlink:href=\"" + bg + "\" width=\"120\" height=\"120\" x=\"10\" y=\"10\"/>\n" +
                "    <image xlink:href=\"" + pic + "\" x=\"30\" y=\"30\" height=\"80\" width=\"80\" clip-path=\"url(#songImagePath)\">\n" +
                "    \n" +
                "    <animateTransform\n" +
                "                      attributeName=\"transform\"\n" +
                "                      attributeType=\"XML\"\n" +
                "                      type=\"rotate\"\n" +
                "                      from=\"0 70 70\"\n" +
                "                      to=\"360 70 70\"\n" +
                "                      dur=\"30\"\n" +
                "                      repeatCount=\"indefinite\" />\n" +
                "    </image>\n" +
                "    \n" +
                "    <image xlink:href=\"" + bar + "\" height=\"80\" x=\"64\" y=\"0\"/>\n" +
                "</svg> \n";

        outputStream.write(str.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
