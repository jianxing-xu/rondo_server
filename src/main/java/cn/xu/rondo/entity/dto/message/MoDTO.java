package cn.xu.rondo.entity.dto.message;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class MoDTO {
    @NotNull(message = "你想摸谁？")
    private Integer at;
    @NotNull
    private Integer room_id;
}
