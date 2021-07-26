package cn.xu.rondo.entity.vo;

import cn.hutool.http.HtmlUtil;
import lombok.Data;

@Data
public class LrcLine {
    private String lineLyric;
    private String time;

    public void setLineLyric(String lineLyric) {
        this.lineLyric = HtmlUtil.escape(lineLyric);
    }
}