package cn.xu.rondo.entity.dto.song;

import lombok.Getter;

import javax.validation.constraints.NotNull;

/**
 *      * @param mid    歌曲mid 页面上可能是rid
 *      * @param roomId 房间id
 *      * @param at     送给谁？
 */
@Getter
public class AddSongDTO {
    @NotNull
    private Long mid;
    @NotNull
    private Integer roomId;
    private Integer atUser;
}
