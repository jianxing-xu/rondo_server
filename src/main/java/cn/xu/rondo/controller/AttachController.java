package cn.xu.rondo.controller;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.generator.UUIDGenerator;
import cn.hutool.core.util.RandomUtil;
import cn.xu.rondo.enums.ErrorEnum;
import cn.xu.rondo.response.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.DateUtils;

import java.io.File;
import java.time.LocalDateTime;

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

    @Value("${rondo.avatar-path}")
    String avatarPath;

    @Value("${server.servlet.context-path}")
    String contextPath;

    @PostMapping("/uploadAvatar")
    public String uploadHead(@RequestParam("avatar") MultipartFile file) {
        if (file.isEmpty()) throw new ApiException(ErrorEnum.FILE_EMPTY);
        LocalDateTime now = LocalDateTimeUtil.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        System.out.println("文件类型ContentType:" + file.getContentType());
        System.out.println("文件组件名称Name:" + file.getName());
        System.out.println("文件大小:" + file.getSize());
        System.out.println("文件原名称OriginalFileName:" + file.getOriginalFilename());
        // TODO:::::-------------------------------------
        try {
            String dayPath = String.format("%s/%s/%s/", year, month, day);
            //上传目录地址
            String uploadDir = avatarPath + dayPath;
            System.out.println(uploadDir);
            //如果目录不存在，自动创建文件夹
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            //遍历文件数组执行上传
            //文件后缀名
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            //上传文件名
            String filename = UUID.randomUUID() + suffix;
            //服务器端保存的文件对象
            File serverFile = new File(uploadDir + filename);
            //将上传的文件写入到服务器端文件内
            file.transferTo(serverFile);

            return avatarPath + dayPath + filename;
        } catch (Exception e) {
            //打印错误堆栈信息
            e.printStackTrace();
            throw new ApiException(ErrorEnum.FILE_UPLOAD_ERR);
        }
    }

    /**
     * 提取上传方法为公共方法
     *
     * @param uploadDir 上传文件目录
     * @param file      上传对象
     * @throws Exception
     */
    private void executeUpload(String uploadDir, MultipartFile file) throws Exception {

    }
}

