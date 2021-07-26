package cn.xu.rondo.service.impl;

import cn.xu.rondo.entity.PlayCount;
import cn.xu.rondo.mapper.PlayCountMapper;
import cn.xu.rondo.service.IPlayCountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PlayCountServiceImpl extends ServiceImpl<PlayCountMapper, PlayCount> implements IPlayCountService {

}

