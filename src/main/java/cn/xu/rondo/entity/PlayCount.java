package cn.xu.rondo.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("playcount")
@Data
public class PlayCount {
    private Integer mid;
    private Integer user_id;
    private Integer played;
}
