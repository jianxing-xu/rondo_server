package cn.xu.rondo.entity.dto;

import cn.xu.rondo.enums.SwitchEnum;
import cn.xu.rondo.enums.RoomType;
import cn.xu.rondo.utils.validation.EnumValue;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateRoom {
    @NotBlank(message = "房间名不能为空")
    @Length(min = 1, max = 20)
    private String room_name;
    @Length(max = 50)
    @NotNull(message = "房间公告不能为空")
    private String room_notice;

    @EnumValue(enumClass = RoomType.class, message = "房间类型不匹配")
    @NotNull(message = "房间类型不能为空")
    private Integer room_type;

    @EnumValue(enumClass = SwitchEnum.class, message = "是否公开枚举不匹配")
    @NotNull(message = "是否公开 不能为空")
    private Integer room_public;

    @EnumValue(enumClass = SwitchEnum.class, message = "跳过点歌枚举不匹配")
    @NotNull(message = "是否投票跳过不能为空")
    private Integer room_votepass;

    @NotNull(message = "投票占比比例不能为空")
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
}
