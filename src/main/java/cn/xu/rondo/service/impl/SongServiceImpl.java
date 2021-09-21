package cn.xu.rondo.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.xu.rondo.entity.Song;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.vo.SearchVo;
import cn.xu.rondo.entity.vo.SongQueueVo;
import cn.xu.rondo.mapper.SongMapper;
import cn.xu.rondo.service.ISongService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    IUserService userService;

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

    @Override
    public SongQueueVo getRandSongByUser(Integer userId) {
        User user = userService.getById(userId);
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        wrapper.eq("song_user",userId);
        List<Song> songs = mapper.selectList(wrapper);
        if (songs != null && songs.size() != 0) {
            Song song = songs.get(RandomUtil.randomInt(0, songs.size() - 1));
            SongQueueVo songQueueVo = new SongQueueVo();
            songQueueVo.setUser(user);
            //searchVo
            SearchVo vo = new SearchVo();
            vo.setName(song.getSong_name());
            vo.setPic(song.getSong_pic());
            vo.setSinger(song.getSong_singer());
            vo.setLength(song.getSong_length());
            vo.setMid(song.getSong_mid());
            songQueueVo.setSong(vo);
            // 设置播放时间
            songQueueVo.setSince(Common.time());
            return songQueueVo;
        }
        return null;
    }


}
