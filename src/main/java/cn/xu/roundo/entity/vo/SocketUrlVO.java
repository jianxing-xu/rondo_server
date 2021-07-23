package cn.xu.roundo.entity.vo;


import cn.hutool.crypto.SecureUtil;
import lombok.Data;

@Data
public class SocketUrlVO {
    private String account;
    private String channel;
    private String ticker;

//    public String md5() {
//        return SecureUtil.md5("account" + account + "channel" + channel + "salt" + channel);
//    }
}
