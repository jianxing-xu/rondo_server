package cn.xu.rondo.entity.dto;

import cn.xu.rondo.enums.RoomType;
import cn.xu.rondo.enums.SwitchEnum;
import cn.xu.rondo.utils.validation.EnumValue;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateRoomDTO {
    @NotBlank(message = "名字不能为空")
    @Length(max = 20)
    private String room_name;

    @NotBlank(message = "房间通知不能为空")
    @Length(max = 50)
    private String room_notice;

    @NotNull(message = "房间类型不能为空")
    @EnumValue(enumClass = RoomType.class, message = "房间类型不匹配")
    private Integer room_type;

    @NotNull(message = "房间是否加密不能为空")
    @EnumValue(enumClass = SwitchEnum.class, message = "房间公开枚举不匹配")
    private Integer room_public;

    @Length(min = 4, max = 16)
    private String room_password;

    @EnumValue(enumClass = SwitchEnum.class, message = "是否投片跳过枚举不匹配")
    private Integer room_votepass;

    @Range(max = 100)
    private Integer room_votepercent;

    @EnumValue(enumClass = SwitchEnum.class, message = "点歌权限枚举不匹配")
    @NotNull(message = "点歌权限不能为空1：房主，0：都可")
    private Integer room_addsong;

    @EnumValue(enumClass = SwitchEnum.class, message = "禁言枚举不匹配")
    @NotNull(message = "是否禁言不能为空1：禁言")
    private Integer room_sendmsg;

    @NotNull(message = "是否要机器人不能为空")
    @EnumValue(enumClass = SwitchEnum.class, message = "是否要机器人枚举不匹配")
    private Integer room_robot;

    @Range(min = 3, max = 60, message = "点歌cd不在范围内")
    private Integer room_addsongcd;

    @Range(min = 3, max = 60, message = "顶歌cd不在范围内")
    private Integer room_pushsongcd;

    @Range(min = 1, max = 10, message = "顶歌日限额不在范围内")
    private Integer room_pushdaycount;

    @Range(min = 1, max = 10, message = "房间点歌数量不在范围内")
    private Integer room_addcount;

    private String room_background;

    // 插件机制

    @NotNull(message = "房间ID不能为空")
    private Integer room_id;

    public boolean isPublic() {
        return room_public == 0;
    }
}
