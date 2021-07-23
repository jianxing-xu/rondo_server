package cn.xu.roundo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 关键词表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_keywords")
public class Keywords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "keywords_id", type = IdType.AUTO)
    private Integer keywords_id;

    /**
     * 原串
     */
    private String keywords_source;

    /**
     * 替换
     */
    private String keywords_target;

    /**
     * 全替换
     */
    private Integer keywords_all;

    /**
     * 状态
     */
    private Integer keywords_status;

    /**
     * 创建时间
     */
    private Integer keywords_createtime;

    /**
     * 修改时间
     */
    private Integer keywords_updatetime;


    public Integer getKeywords_id() {
        return keywords_id;
    }

    public void setKeywords_id(Integer keywords_id) {
        this.keywords_id = keywords_id;
    }

    public String getKeywords_source() {
        return keywords_source;
    }

    public void setKeywords_source(String keywords_source) {
        this.keywords_source = keywords_source;
    }

    public String getKeywords_target() {
        return keywords_target;
    }

    public void setKeywords_target(String keywords_target) {
        this.keywords_target = keywords_target;
    }

    public Integer getKeywords_all() {
        return keywords_all;
    }

    public void setKeywords_all(Integer keywords_all) {
        this.keywords_all = keywords_all;
    }

    public Integer getKeywords_status() {
        return keywords_status;
    }

    public void setKeywords_status(Integer keywords_status) {
        this.keywords_status = keywords_status;
    }

    public Integer getKeywords_createtime() {
        return keywords_createtime;
    }

    public void setKeywords_createtime(Integer keywords_createtime) {
        this.keywords_createtime = keywords_createtime;
    }

    public Integer getKeywords_updatetime() {
        return keywords_updatetime;
    }

    public void setKeywords_updatetime(Integer keywords_updatetime) {
        this.keywords_updatetime = keywords_updatetime;
    }

    @Override
    public String toString() {
        return "Keywords{" +
        "keywords_id=" + keywords_id +
        ", keywords_source=" + keywords_source +
        ", keywords_target=" + keywords_target +
        ", keywords_all=" + keywords_all +
        ", keywords_status=" + keywords_status +
        ", keywords_createtime=" + keywords_createtime +
        ", keywords_updatetime=" + keywords_updatetime +
        "}";
    }
}
