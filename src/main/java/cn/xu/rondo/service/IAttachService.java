package cn.xu.rondo.service;

import cn.xu.rondo.entity.Attach;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 附件表 服务类
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
public interface IAttachService extends IService<Attach> {
    void checkAvatarType(MultipartFile file);

    String upload(MultipartFile file, String dir) throws IOException;

    String uploadImg(MultipartFile file, String dir, int type) throws IOException;

    String getDayPath();

    Attach checkFileExist(String sha);

    void checkMusicType(MultipartFile file);
}
