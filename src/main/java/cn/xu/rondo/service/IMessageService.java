package cn.xu.rondo.service;

import cn.xu.rondo.entity.Message;
import cn.xu.rondo.entity.vo.MessageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 消息表 服务类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface IMessageService extends IService<Message> {

    boolean checkIPBAN();

    boolean checkTimeIn();

    List<MessageVO> selectMessages(Page<MessageVO> page, Integer to, Integer status);
}
