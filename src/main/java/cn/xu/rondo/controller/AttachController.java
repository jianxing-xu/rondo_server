package cn.xu.rondo.controller;


import cn.hutool.crypto.digest.DigestUtil;
import cn.xu.rondo.entity.Attach;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IAttachService;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.params_resolver.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
/**
 * <p>
 * 附件表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Slf4j
@RestController
@RequestMapping("/attach")
public class AttachController extends BaseController {

    // 图片在系统中真实地址
    @Value("${rondo.avatar-path}")
    String avatarPath;

    @Value("${server.servlet.context-path}")
    String contextPath;

    @Autowired
    IAttachService attachService;

    /**
     * 上传头像
     * @param file  头像文件
     * @param userId 用户id
     * @return attach对象
     */
    @PostMapping("/uploadAvatar")
    public Attach uploadHead(@RequestParam("avatar") MultipartFile file,
                             @UserId Integer userId) {
        try {
            attachService.checkAvatarType(file);
            //比对 sha 摘要值
            String sha = new String(DigestUtil.sha1(file.getBytes()));
            Attach attach = attachService.checkFileExist(sha);
            if (attach == null) {
                String dayPath = attachService.getDayPath();
                //上传目录地址
                String uploadDir = avatarPath + dayPath;
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
                String filename = attachService.upload(file, uploadDir);
                // 图片的可访问地址
                String finallyPath = "/res/avatar/" + dayPath + filename;
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
//    @PostMapping("/uploadMusic")
//    public Attach uploadHead(@RequestParam("avatar") MultipartFile file,
//                             @UserId Integer userId) {
//
//    }
}

