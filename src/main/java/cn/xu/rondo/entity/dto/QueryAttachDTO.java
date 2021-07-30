package cn.xu.rondo.entity.dto;


import cn.xu.rondo.utils.StringUtils;
import lombok.Getter;

import javax.validation.constraints.Pattern;

@Getter
public class QueryAttachDTO extends BaseDTO {

    @Pattern(regexp = "\\d+,\\d+")
    private String sizePattern = "0,9999999999"; // 大小规则，默认 大于0kb

    private String type; // 附件类型


    public Long minSize() {
        final String[] split = this.sizePattern.split(",");
        return "~".equals(split[0]) ? 0 : Long.parseLong(split[0]);
    }

    public Long maxSize() {
        final String[] split = this.sizePattern.split(",");
        return "~".equals(split[1]) ? 9999999999L : Long.parseLong(split[1]);
    }

}
