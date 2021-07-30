package cn.xu.rondo.controller;


import cn.hutool.crypto.digest.DigestUtil;
import cn.xu.rondo.entity.Attach;
import cn.xu.rondo.entity.dto.QueryAttachDTO;
import cn.xu.rondo.entity.vo.QueryVo;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IAttachService;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.params_resolver.UserId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * <p>
 * 附件表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/attach")
public class AttachController extends BaseController {

    @Value("${rondo.upload-path}")
    String uploadPath; // /Users/...../
    @Value("${rondo.avatar-path}")
    String avatarPath; // avatar/
    @Value("${rondo.chat-path}")
    String chatPath; // chat/
    @Value("${rondo.music-path}")
    String musicPath; // music/


    @Value("${server.servlet.context-path}")
    String contextPath;

    @Autowired
    IAttachService attachService;

    /**
     * 上传头像
     *
     * @param file   头像文件
     * @param userId 用户id
     * @param type   0:头像，1:聊天图片
     * @return attach对象
     */
    @PostMapping("/uploadImg")
    public Attach upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("type") Integer type, //0:头像，1:聊天图片
                         @UserId Integer userId) {
        try {
            attachService.checkAvatarType(file);
            //比对 sha 摘要值
            String sha = new String(DigestUtil.sha1(file.getBytes()));
            Attach attach = attachService.checkFileExist(sha);
            if (attach == null) {
                String dayPath = attachService.getDayPath();
                //上传目录地址
                String uploadDir = uploadPath + (type.equals(0) ? avatarPath : chatPath) + dayPath;
                System.out.println(uploadDir);
                //如果目录不存在，自动创建文件夹
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    boolean b = dir.mkdirs();
                    if (!b) {
                        log.error("目录创建失败");
                        throw new ApiException(EE.MKDIR_ERR);
                    }
                }
                // 上传
                String filename = attachService.uploadImg(file, uploadDir, type);
                // 图片的可访问地址
                String finallyPath = "/res/" + (type.equals(0) ? avatarPath : chatPath) + dayPath + filename;
                //插入数据库
                Attach newAvatar = new Attach();
                newAvatar.setAttach_path(finallyPath);
                newAvatar.setAttach_sha(sha);
                newAvatar.setAttach_size(file.getSize());
                newAvatar.setAttach_createtime(Common.time().intValue());
                newAvatar.setAttach_updatetime(Common.time().intValue());
                newAvatar.setAttach_type(file.getContentType());
                newAvatar.setAttach_user(userId);
                attachService.save(newAvatar);
                return newAvatar;
            }
            return attach;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            //打印错误堆栈信息
            e.printStackTrace();
            throw new ApiException(EE.FILE_UPLOAD_ERR);
        }
    }

    //TODO: Coming soon...
    @PostMapping("/uploadMusic")
    public Attach uploadHead(@RequestParam("file") MultipartFile file,
                             @UserId Integer userId) {
        try {
            attachService.checkMusicType(file);
            //比对 sha 摘要值
            String sha = new String(DigestUtil.sha1(file.getBytes()));
            Attach attach = attachService.checkFileExist(sha);
            if (attach == null) {
                String dayPath = attachService.getDayPath();
                //上传目录地址
                String uploadDir = uploadPath + musicPath + dayPath;
                //如果目录不存在，自动创建文件夹
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    boolean b = dir.mkdirs();
                    if (!b) {
                        log.error("目录创建失败");
                        throw new ApiException(EE.MKDIR_ERR);
                    }
                }
                // 上传
                String filename = attachService.upload(file, uploadDir);
                // 图片的可访问地址
                String finallyPath = "/res/" + musicPath + dayPath + filename;
                //插入数据库
                Attach music = new Attach();
                music.setAttach_path(finallyPath);
                music.setAttach_sha(sha);
                music.setAttach_size(file.getSize());
                music.setAttach_createtime(Common.time().intValue());
                music.setAttach_updatetime(Common.time().intValue());
                music.setAttach_type(file.getContentType());
                music.setAttach_user(userId);
                attachService.save(music);
                return music;
            }
            return attach;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            //打印错误堆栈信息
            e.printStackTrace();
            throw new ApiException(EE.FILE_UPLOAD_ERR);
        }
    }


    //==TODO:后台接口==============================================
    @PostMapping("/all")
    public QueryVo<Attach> all(@RequestBody QueryAttachDTO dto) {
        Page<Attach> pager = new Page<>(dto.getPageNum(), dto.getPageSize());
        Attach query = new Attach();
        query.setAttach_type(dto.getType());
        query.setAttach_user(dto.getUserId());
        query.setAttach_status(dto.getStatus());

        QueryWrapper<Attach> wrapper = new QueryWrapper<>(query);
        final String sizePattern = dto.getSizePattern();
        if (sizePattern != null) {
            wrapper.gt("attach_size", dto.minSize());
            wrapper.lt("attach_size", dto.maxSize());
        }
        final Page<Attach> page = attachService.page(pager, wrapper);
        List<Attach> records = page.getRecords();
        long total = page.getTotal();
        final QueryVo<Attach> vo = new QueryVo<>();
        vo.setList(records);
        vo.setTotal(total);
        return vo;
    }
}

