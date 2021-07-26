package cn.xu.rondo.service.impl;

import cn.xu.rondo.entity.Keywords;
import cn.xu.rondo.mapper.KeywordsMapper;
import cn.xu.rondo.service.IKeywordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 关键词表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class KeywordsServiceImpl extends ServiceImpl<KeywordsMapper, Keywords> implements IKeywordsService {

}
