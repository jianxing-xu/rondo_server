package cn.xu.rondo.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.xu.rondo.entity.vo.SearchVo;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IConfService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Arrays.*;


@Component("KwUtils")
public class KwUtils {

    @Value("${rondo.static-url}")
    String staticUrl;

    @Autowired
    RedisUtil redis;

    @Autowired
    IConfService confService;

    public static final Logger log = LoggerFactory.getLogger(KwUtils.class);


    public SearchVo getRandomSong() {
        final String randomBangIds = confService.get("BANG_IDS");
        List<String> bangIds = stream(randomBangIds.split(",")).filter(it -> !it.equals("")).collect(Collectors.toList());
        if(bangIds.size() <= 0) {
            bangIds = ListUtil.of("93", "17", "16", "158", "145", "284", "187", "26", "185", "278", "104", "151");
        }
        String bangId = bangIds.get(RandomUtil.randomInt(0, bangIds.size() - 1));
        String token = RandomUtil.randomNumbers(8);
        try {
            String body = HttpRequest.get("http://kuwo.cn/api/www/bang/bang/musicList?bangId=" + bangId + "&pn=1&rn=100")
                    .header("csrf", token)
                    .cookie("kw_token=" + token)
                    .timeout(5000)
                    .execute()
                    .body();
            JSONObject json = JSONObject.parseObject(body);
            if ("200".equals(String.valueOf(json.get("code")))) {
                JSONArray array = json.getJSONObject("data").getJSONArray("musicList");
                JSONObject song = array.getJSONObject(RandomUtil.randomInt(0, array.size() - 1));
                SearchVo vo = new SearchVo();
                vo.setMid(Long.parseLong(String.valueOf(song.get("rid"))));
                vo.setName((String) song.get("name"));
                vo.setPic((String) song.get("pic"));
                vo.setLength((Integer) song.get("duration"));
                vo.setSinger((String) song.get("artist"));
                return vo;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            log.error("拉取歌曲超时");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public List<String> getKwSearchKey() {
        List<String> cacheList = redis.getCacheList(Constants.KwHotKey);
        if (cacheList != null && cacheList.size() != 0) return cacheList;
        String token = RandomUtil.randomNumbers(8);
        try {
            final String body = HttpRequest.get("http://bd.kuwo.cn/api/www/search/searchKey?key=&httpsStatus=1")
                    .header("Referer", "http://bd.kuwo.cn")
                    .header("csrf", token)
                    .cookie("kw_token=" + token)
                    .timeout(5000)
                    .execute()
                    .body();
            if (body != null && StringUtils.isNotEmpty(body)) {
                JSONObject jsonObject = JSONObject.parseObject(body);
                Object[] keys = jsonObject.getJSONArray("data").toArray();
                List<String> objects = Convert.toList(String.class, keys);
                if (objects != null) {
                    redis.setCacheList(Constants.KwHotKey, objects);
                    redis.expire(Constants.KwHotKey, 1, TimeUnit.HOURS);
                }
                return objects;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(EE.KW_QUERY_ERR);
        }
        return new ArrayList<String>() {{
            add("周杰伦");
            add("林俊杰");
            add("张学友");
            add("林志炫");
            add("毛不易");
            add("朴树");
        }};
    }

    public List<SearchVo> getSearchResult(String keyword, int page) {
        String token = RandomUtil.randomNumbers(8);
        List<SearchVo> searchResult = new ArrayList<>();
        // 取到搜索结果缓存直接返回
        List<SearchVo> cacheList = redis.getCacheList(Constants.SearchHistoryResult + keyword);
        if (cacheList != null && cacheList.size() != 0) return cacheList;
        try {
            final String body = HttpRequest.get("http://bd.kuwo.cn/api/www/search/searchMusicBykeyWord?key=" + keyword + "&pn=" + page + "&rn=" + 50)
                    .header("Referer", "http://bd.kuwo.cn")
                    .header("csrf", token)
                    .cookie("kw_token=" + token)
                    .timeout(5000)
                    .execute()
                    .body();
            if (body != null && StringUtils.isNotEmpty(body)) {
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                if (data == null) {
                    return searchResult;
                }
                JSONArray list = data.getJSONArray("list");
                list.forEach((song) -> {
                    //String songPic = staticUrl+"images/logo.png";
                    JSONObject json = (JSONObject) JSON.toJSON(song);
                    SearchVo searchVo = new SearchVo();
                    searchVo.setMid((Long.parseLong((json.get("rid").toString()))));
                    searchVo.setName(HtmlUtil.unescape((String) json.get("name")));
                    searchVo.setPic((String) json.get("pic"));
                    searchVo.setLength((Integer) json.get("duration"));
                    searchVo.setSinger(HtmlUtil.unescape((String) json.get("artist")));
                    searchVo.setAlbum((String) json.get("album"));
                    searchResult.add(searchVo);
                    //缓存所有歌曲详情
                    redis.setCacheObject(Constants.SongDetail + searchVo.getMid(), searchVo);
                    redis.expire(Constants.SongDetail + searchVo.getMid(), 1, TimeUnit.HOURS);
                });
                // 对搜索结果缓存，下次用相同搜索结果时，直接取缓存，有效期为1分钟
                redis.setCacheList(Constants.SearchHistoryResult + keyword, searchResult);
                redis.expire(Constants.SearchHistoryResult + keyword, 1, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(EE.KW_QUERY_ERR);
        }
        return searchResult;
    }

    public String getPlayUrl(Long mid) {
        try {
//            https://m.kuwo.cn/newh5app/api/mobile/v1/music/src/228908
            //http://bd.kuwo.cn/url?rid=' . $mid . '&type=convert_url3&br=128kmp3
            String body = HttpRequest.get("http://bd.kuwo.cn/api/v1/www/music/playUrl?mid=" + mid + "&type=music&httpsStatus=1")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36")
                    .timeout(5000)
                    .execute().body();
            JSONObject json = JSON.parseObject(body);
            if ("200".equals(String.valueOf(json.get("code")))) {
                if (StringUtils.isNotEmpty(String.valueOf(json.get("data")))) {
                    return String.valueOf(json.getJSONObject("data").get("url"));
                }
            } else {
                body = HttpRequest.get("https://m.kuwo.cn/newh5app/api/mobile/v1/music/src/" + mid)
                        .timeout(5000)
                        .execute().body();
                json = JSON.parseObject(body);
                if ("200".equals(String.valueOf(json.get("code")))) {
                    if (StringUtils.isNotEmpty(String.valueOf(json.get("data")))) {
                        return String.valueOf(json.getJSONObject("data").get("url"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(EE.KW_QUERY_ERR);
        }
        return null;
    }
}
