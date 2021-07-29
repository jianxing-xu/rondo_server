package cn.xu.rondo.entity.vo;


import lombok.Data;

import java.util.List;

@Data
public class QueryVo<T> {
    private List<T> list;
    private Long total;
}
