package cn.xu.rondo.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HotRoomVO {
    private Integer room_addcount;//: 3
    private Integer room_addsong;//: 0
    private Integer room_addsongcd;//: 10
    private String room_app;//: "https://test.hamm.cn/ac/"
    private String room_background;//: ""
    private Integer room_createtime;//: 1598539777
    private Integer room_fullpage;//: 0
    private Integer room_hide;//: 0
    private Integer room_id;//: 888
    private String room_name;//: "BBBUG音乐大厅"
    private String room_notice;//: "BBBUG插件开放啦,快去开发你自己房间的插件吧~"
    private Integer room_online;//: 19
    private Integer room_order;//: 100000000
    private Integer room_playone;//: 1
    private Integer room_public;//: 0
    private Integer room_pushdaycount;//: 5
    private Integer room_pushsongcd;//: 600
    private Integer room_realonline;//: 33
    private String room_reason;//: ""
    private Integer room_robot;//: 0
    private Integer room_sendmsg;//: 0
    private Integer room_status;//: 0
    private Integer room_type;//: 1
    private Integer room_updatetime;//: 1628060764
    private Integer room_user;//: 1
    private Integer room_votepass;//: 1
    private Integer room_votepercent;//: 20
    private Boolean user_admin;//: true
    private Integer user_group;//: 1
    private String user_head;//: "https://bbbug.hamm.cn//uploads/thumb/image/20210303/64967b21cec643e6ea434edb7d375a4b.png"
    private Integer user_id;//: 1
    private String user_name;//: "%E6%9C%BA%E5%99%A8%E4%BA%BA"

    public void setUser_admin() {
        List<Integer> admins = new ArrayList<Integer>() {{
            add(1);
        }};
        this.user_admin = admins.contains(user_group);
    }
}