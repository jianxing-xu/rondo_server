package cn.xu.rondo.entity.dto.song;


import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
public class AddNewSongDTO {
    @NotNull
    private Long songMid;
    @NotEmpty
    private String songName;
    @NotEmpty
    private String songSinger;
    @NotEmpty
    private String songPic;
    @NotNull
    private Integer songLength;
}
