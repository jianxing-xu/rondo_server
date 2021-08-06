package cn.xu.rondo.service.impl;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.xu.rondo.entity.Message;
import cn.xu.rondo.entity.vo.MessageVO;
import cn.xu.rondo.mapper.MessageMapper;
import cn.xu.rondo.service.IMessageService;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.RedisUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    @Autowired
    RedisUtil redis;

    @Autowired
    MessageMapper mapper;

    @Override
    public boolean checkIPBAN() {
        List<String> ipList = redis.getCacheList(Constants.IPBanList);
        if (ipList != null && ipList.size() != 0) {
            String ip = Common.getIpAddr();
            return ipList.contains(ip);
        }
        return false;
    }

    @Override
    public boolean checkTimeIn() {
        long now = System.currentTimeMillis();
        final Calendar start = CalendarUtil.calendar();
        start.set(Calendar.HOUR_OF_DAY, 18);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        final Calendar end = CalendarUtil.calendar();
        end.set(Calendar.HOUR_OF_DAY, 9);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);

        Date endDate = DateUtil.offset(DateUtil.date(end), DateField.DAY_OF_MONTH, 1);
        return now > start.getTimeInMillis() && now < endDate.getTime();
    }

    /**
     * 查询消息记录
     * @param page 分页对象
     * @return 消息列表
     */
    @Override
    public List<MessageVO> selectMessages(Page<MessageVO> page, Integer to, Integer status) {
        return mapper.selectMessages(page,to,status);
    }
}
