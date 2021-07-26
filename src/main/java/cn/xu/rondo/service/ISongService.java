package cn.xu.rondo.service;

import cn.xu.rondo.entity.Song;
import cn.xu.rondo.entity.vo.SearchVo;
import cn.xu.rondo.entity.vo.SongQueueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 歌曲表 服务类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface ISongService extends IService<Song> {
    List<SearchVo> getWeekHot();

    Song getOneByMap(Map<String, Object> data);

    SearchVo getSongDetail(Long mid);

    SongQueueVo getRandSongByUser(Integer userId);
}
