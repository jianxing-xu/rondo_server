package cn.xu.rondo.mapper;

import cn.xu.rondo.entity.Message;
import cn.xu.rondo.entity.vo.MessageVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * <p>
 * 消息表 Mapper 接口
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface MessageMapper extends BaseMapper<Message> {
    List<MessageVO> selectMessages(Page<?> page, Integer to, Integer status);
}
