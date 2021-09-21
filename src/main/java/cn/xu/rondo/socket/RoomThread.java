package cn.xu.rondo.socket;

import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpException;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.vo.SongQueueVo;
import cn.xu.rondo.task.SongTask;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;


@Slf4j
public class RoomThread extends Thread {
    private Integer roomId;

    public RoomThread(Integer roomId) {
        // 设置线程名
        setName(Constants.RoomThreadPREFIX + roomId);
        this.roomId = roomId;
    }

    public static SongTask songTask = SpringUtils.getBean(SongTask.class);

    // 线程标志位 需改为true时退出线程
    public volatile boolean exited = false;

    public void exit() {
        exited = true;
    }

    @Override
    public void run() {
        while (!exited) {
            Room room = SongTask.rooms.get(roomId);
            try {
                sleep(2000);
                SongQueueVo songQueueVo = songTask.getPlaying(room.getRoom_id());
                // 判断是否有正在播放的歌曲
                if (songQueueVo != null && songQueueVo.getSong() != null) {
                    // 当前时间戳小于 歌曲的开始播放时间 + 歌曲的长度表示歌曲还没有播放完，正在播放中.....
                    if (Common.time() < songQueueVo.getSong().getLength() + songQueueVo.getSince()) {
                        // 点一首歌到队列中
                        songTask.getSongByRobot(room);
                        continue;
                    }
                }
                // 从队列中pop
                songQueueVo = songTask.popSong(room.getRoom_id(), room.isRadioStation());
                if (songQueueVo != null) {
                    // 弹出了队列中的歌曲 直接播放
                    songTask.play(room.getRoom_id(), songQueueVo);
                }
                songTask.getSongByRobot(room);

            } catch (HttpException e) {
                log.error("房间" + room.getRoom_name() + "网络异常，正在重连.....");
            } catch (RedisSystemException e) {
                log.error("redis 异常");
            } catch (InterruptedException e) {
                log.error(room.getRoom_id() + "房间线程停止了");
                if (isInterrupted()) {
                    log.info(room.getRoom_name() + "终止了！");
                    break;
                }
                break;
            } catch (Exception e) {
                // 业务异常
                e.printStackTrace();
                try {
                    songTask.redis.setCacheObject(Constants.SongNow + room.getRoom_id(), null);
                } catch (Exception innerE) {
                    log.error("中断程序redis异常");
                }
            }

        }
    }
}