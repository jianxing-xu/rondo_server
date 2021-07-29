package cn.xu.rondo.entity.dto;


import lombok.Getter;

import javax.validation.constraints.Pattern;

@Getter
public class QueryAttachDTO extends BaseDTO {

    @Pattern(regexp = "\\d+,\\d+")
    private String sizePattern = "0,9999999999"; // 大小规则，默认 大于0kb

    private String type; // 附件类型

}
