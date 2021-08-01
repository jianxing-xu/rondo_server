package cn.xu.rondo.entity.vo;


import lombok.Data;

@Data
public class SocketUrlVO {
    private String account;
    private String channel;
    private String ticket;

//    public String md5() {
//        return SecureUtil.md5("account" + account + "channel" + channel + "salt" + channel);
//    }
}
