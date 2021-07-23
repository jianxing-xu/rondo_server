package cn.xu.roundo.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.xu.roundo.entity.Song;
import cn.xu.roundo.entity.vo.SearchVo;
import cn.xu.roundo.mapper.SongMapper;
import cn.xu.roundo.service.ISongService;
import cn.xu.roundo.utils.Constants;
import cn.xu.roundo.utils.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 歌曲表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements ISongService {

    @Autowired
    SongMapper mapper;

    @Autowired
    RedisUtil redis;

    @Override
    public List<SearchVo> getWeekHot() {
        return mapper.getWeekHot();
    }

    @Override
    public Song getOneByMap(Map<String, Object> data) {
        List<Song> songs = mapper.selectByMap(data);
        if (songs.size() == 0) {
            return null;
        }
        return songs.get(0);
    }

    public SearchVo getSongDetail(Long mid) {
        SearchVo detail = redis.getCacheObject(Constants.SongDetail + mid);
        if (detail == null) {
            List<Song> songs = mapper.selectByMap(new HashMap<String, Object>() {{
                put("song_mid", mid);
            }});
            if (songs.size() == 0) return null;
            detail = new SearchVo();
            Song song = songs.get(0);
            detail.setSinger(song.getSong_singer());
            detail.setPic(song.getSong_pic());
            detail.setLength(song.getSong_length());
            detail.setName(song.getSong_name());
            detail.setMid(song.getSong_mid());
        }
        return detail;
    }


}
