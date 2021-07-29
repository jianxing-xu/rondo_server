package cn.xu.rondo.entity.dto;


import cn.xu.rondo.enums.SwitchEnum;
import cn.xu.rondo.utils.validation.EnumValue;
import lombok.Data;

@Data
public class BaseDTO {
    private Integer userId; // 用户id

    private Integer pageNum = 20;

    private Integer pageSize = 1;

    private Long startAt;  // 开始时间

    private Long endAt; // 结束时间

    @EnumValue(enumClass = SwitchEnum.class, message = "状态类型不匹配")
    private Integer status; // 状态
}
