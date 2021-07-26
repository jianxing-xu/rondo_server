package cn.xu.rondo.service.impl;

import cn.xu.rondo.entity.Message;
import cn.xu.rondo.mapper.MessageMapper;
import cn.xu.rondo.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

}
