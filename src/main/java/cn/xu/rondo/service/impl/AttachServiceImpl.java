package cn.xu.rondo.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.xu.rondo.entity.Attach;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.mapper.AttachMapper;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IAttachService;
import cn.xu.rondo.utils.ImgUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.imageio.plugins.common.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Slf4j
@Service
public class AttachServiceImpl extends ServiceImpl<AttachMapper, Attach> implements IAttachService {

    @Autowired
    AttachMapper mapper;

    @Value("${rondo.upload-img-max}")
    String imgMax;

    @Value("${rondo.upload-music-max}")
    String musicMax;

    @Override
    public void checkAvatarType(MultipartFile file) {
        if (file.isEmpty()) throw new ApiException(EE.FILE_EMPTY);
        int limit = Integer.parseInt(imgMax.replace("MB", ""));
        // 检查大小
        long size = file.getSize();
        if (size / 1024 / 1024 > limit) throw new ApiException(EE.AVA_MAX_1M);
        //检查类型
        String type = file.getContentType();
        if (!"image/jpeg".equals(type) && !"image/png".equals(type))
            throw new ApiException(EE.FILE_TYPE_ERR);
    }

    @Override
    public String upload(MultipartFile file, String dir) throws IOException {
        //遍历文件数组执行上传
        //文件后缀名
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //上传文件名
        String filename = UUID.randomUUID() + suffix;
        //服务器端保存的文件对象
        File serverFile = new File(dir + filename);
        //将上传的文件写入到服务器端文件内
        file.transferTo(serverFile);
        return filename;
    }

    // 图片保存前压缩
    @Override
    public String uploadImg(MultipartFile file, String dir, int type) throws IOException {
        //文件后缀名
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //上传文件名
        String filename = UUID.randomUUID() + suffix;
        //服务器端保存的文件对象
//        File serverFile = FileUtil.file(dir + filename);
        File saveFile = new File(dir + filename);
        Thumbnails.of(file.getInputStream())
                .size(400, 400) // 限制了大小后就不用压缩了
                //.outputQuality(type == 0 ? 0.8f : 1) // 压缩
                .toFile(saveFile);
        //将上传的文件写入到服务器端文件内
        return filename;
    }

    @Override
    public String getDayPath() {
        LocalDateTime now = LocalDateTimeUtil.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        return String.format("%s/%s/%s/", year, month, day);
    }

    @Override
    public Attach checkFileExist(String sha) {
        log.warn("SHA签名：" + sha);
        QueryWrapper<Attach> wrap = new QueryWrapper<>();
        wrap.eq("attach_sha", sha);
        return mapper.selectOne(wrap);
    }

    @Override
    public void checkMusicType(MultipartFile file) {
        log.info("文件类型："+file.getContentType());
        int limit = Integer.parseInt(musicMax.replace("MB", ""));
        if (file.isEmpty()) throw new ApiException(EE.FILE_EMPTY);
        // 检查大小
        long size = file.getSize();
        if (size / 1024 / 1024 > limit) throw new ApiException(EE.MUSIC_MAX_20);
        //检查类型
        String type = file.getContentType();
        if (!"audio/mp3".equals(type) && !"audio/mpeg".equals(type))
            throw new ApiException(EE.FILE_TYPE_ERR);
    }
}
