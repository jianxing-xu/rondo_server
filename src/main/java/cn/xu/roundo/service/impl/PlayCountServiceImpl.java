package cn.xu.roundo.service.impl;

import cn.xu.roundo.entity.PlayCount;
import cn.xu.roundo.mapper.PlayCountMapper;
import cn.xu.roundo.service.IPlayCountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PlayCountServiceImpl extends ServiceImpl<PlayCountMapper, PlayCount> implements IPlayCountService {

}

