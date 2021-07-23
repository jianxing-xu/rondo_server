package cn.xu.roundo.mapper;

import cn.xu.roundo.entity.Song;
import cn.xu.roundo.entity.vo.SearchVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 歌曲表 Mapper 接口
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface SongMapper extends BaseMapper<Song> {
    @Select("select count(song_week) as week,song_mid as mid,song_id as id,song_pic as pic,song_singer as singer,song_name as name from sa_song where song_week > 0 group by song_mid order by week desc limit 0,50")
    List<SearchVo> getWeekHot();
}
