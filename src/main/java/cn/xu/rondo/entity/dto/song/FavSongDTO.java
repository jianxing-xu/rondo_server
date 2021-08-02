package cn.xu.rondo.entity.dto.song;

import lombok.Getter;

import javax.validation.constraints.NotNull;

/**
 *      * @param mid    歌曲mid
 *      * @param roomId 房间id
 */
@Getter
public class FavSongDTO {
    @NotNull
    private Long mid;
    @NotNull
    private Integer roomId;
}
