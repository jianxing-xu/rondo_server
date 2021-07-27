package cn.xu.rondo.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.UUID;
import cn.xu.rondo.entity.Attach;
import cn.xu.rondo.enums.ErrorEnum;
import cn.xu.rondo.mapper.AttachMapper;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IAttachService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Service
public class AttachServiceImpl extends ServiceImpl<AttachMapper, Attach> implements IAttachService {

    @Autowired
    AttachMapper mapper;

    @Override
    public void checkAvatarType(MultipartFile file) {
        if (file.isEmpty()) throw new ApiException(ErrorEnum.FILE_EMPTY);
        // 检查大小
        long size = file.getSize();
        if (size / 1024 > 1024) throw new ApiException(ErrorEnum.AVA_MAX_1M);
        //检查类型
        String type = file.getContentType();
        if (!"image/jpeg".equals(type) && !"image/png".equals(type))
            throw new ApiException(ErrorEnum.FILE_TYPE_ERR);
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
}
