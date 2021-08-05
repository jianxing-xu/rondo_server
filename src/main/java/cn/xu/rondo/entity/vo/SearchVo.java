package cn.xu.rondo.entity.vo;

import cn.hutool.http.HtmlUtil;
import lombok.Data;

@Data
public class SearchVo {
    private String album;
    private Integer length;
    private Long mid;
    private String name;
    private String pic;
    private String singer;

    //查询数据库的字段
    private Integer id;
    private Integer week;

    public void setName(String name) {
        name = HtmlUtil.unescape(name);
        this.name = name;
    }

    public void setAlbum(String album) {
        album = album.replace("&apos;", "");
        album = HtmlUtil.unescape(album);
        this.album = album;
    }

    public void setSinger(String singer) {
        singer = singer.replace("&apos;", "");
        singer = HtmlUtil.unescape(singer);
        this.singer = singer;
    }
}
