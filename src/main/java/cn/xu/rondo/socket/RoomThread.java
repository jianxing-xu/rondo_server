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
            log.info("----------------------------------------------------");
            try {
                sleep(2000);
                SongQueueVo songQueueVo = songTask.getPlaying(room.getRoom_id());
                // 判断是否有正在播放的歌曲
                if (songQueueVo != null && songQueueVo.getSong() != null) {
                    // 当前时间戳小于 歌曲的开始播放时间 + 歌曲的长度表示歌曲还没有播放完，正在播放中
                    if (Common.time() < songQueueVo.getSong().getLength() + songQueueVo.getSince()) {
                        log.info(String.format("房间：%s 正在播放 %s 中,已经播放了%s秒了", room.getRoom_name(), HtmlUtil.escape(songQueueVo.getSong().getName()), Common.time() - songQueueVo.getSince()));
                        songTask.getSongByRobot(roomId);
                        continue;
                    }

                    // 执行到这里表示歌曲已经播放完了,如果是单曲循环
                    log.info(String.format("歌曲 %s 已经播放完了", songQueueVo.getSong().getName()));
                    //必须 开启电台模式 单曲循环才有效
                    if (room.isSingleCycle() && room.isRadioStation()) {
                        // 重置当前点歌时间为当前时间戳
                        log.info(String.format("房间 %s 开始单曲循环", room.getRoom_name()));
                        songQueueVo.setSince(Common.time());
                        songTask.play(room.getRoom_id(), songQueueVo);
                        continue;
                    }
                }
                // 执行到这里就是，当前房间没有正在播放的歌曲-----------------NO_PLAYING---------------
                // 从队列中弹出一首歌播放
                songQueueVo = songTask.popSong(room.getRoom_id(), room.isRadioStation());
                if (songQueueVo != null) {
                    songTask.play(room.getRoom_id(), songQueueVo);
                    log.info("开始播放 刚刚弹出的歌：" + songQueueVo.getSong().getName());
                    continue;
                }
                log.info("房间 " + room.getRoom_name() + " 队列里没有歌曲拉~");
                // 如果是电台房间，就从用户已点歌曲中随机取一首歌播放
                if (room.isRadioStation()) {
                    songQueueVo = songTask.songService.getRandSongByUser(room.getRoom_user());
                    log.info(String.format("房间 %s 开启了电台模式", room.getRoom_name()));
                    // 如果电台中没有歌曲 就播放不了了
                    if (songQueueVo == null) {
                        log.info("房间 " + room.getRoom_name() + " 的电台也没有歌曲！没有歌曲在播放！！");
                        continue;
                    }
                    log.info(String.format("房间 %s 从电台中拿到了歌曲 %s,并开始播放", room.getRoom_name(), songQueueVo.getSong().getName()));
                    songTask.play(room.getRoom_id(), songQueueVo);
                } else {
                    // 如果不是电台模式，需要判断是否开启机器人点歌
                    log.info(String.format("房间 %s 不是电台模式", room.getRoom_name()));
                    if (room.isRobot()) {
                        log.info(String.format("房间 %s 开启了机器人点歌模式", room.getRoom_name()));
                        SongQueueVo song = songTask.getSongByRobot();
                        if (song == null) {
                            log.info("房间 " + room.getRoom_name() + " 的机器人没点着歌");
                            continue;
                        }
                        log.info(String.format("机器人点着歌了：%s", song.getSong().getName()));
                        songTask.play(room.getRoom_id(), song);
                    } else {
                        log.info("也没有机器人点歌 没在");
                    }
                }
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