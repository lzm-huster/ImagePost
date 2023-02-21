package com.ykq.file.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@RestController
public class FileUploadAndDownloadRest {

    private static final Logger log = LoggerFactory.getLogger(FileUploadAndDownloadRest.class);

    /**
     * @param request 请求request
     * @param file    文件
     * @param imgPath 自定义图床图床路径
     * @description 图床图片上传
     **/
    @RequestMapping("uploadImg")
    public JSONObject uploadImg(HttpServletRequest request,
                                @RequestPart("file") MultipartFile file,
                                @RequestParam("imgPath") String imgPath,
                                @RequestParam("ipAddress") String ipAddress) {
        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        long timeStamp = date.getTime();
        String dateStr = formatter.format(date);
        String format = dateStr + "-" + timeStamp;
        try {
            String ip = getIpAddress(request);
            // 请求值不能为空才能上传
            if (Objects.nonNull(file) && StringUtils.isNoneBlank(imgPath) && StringUtils.isNoneBlank(format) && StringUtils.isNoneBlank(ipAddress)) {
                String originalFilename = file.getOriginalFilename();
                String fileName = imgPath + format + "-" + originalFilename;
                File uploadFile = new File(fileName);
                // 保存文件，使用此方法保存必须要绝对路径且文件夹必须已存在,否则报错
                file.transferTo(uploadFile);
                jsonObject.put("path", ipAddress + format + "-" + originalFilename);

                log.info("来自ip：{} 的请求，文件访问路径：{}", ip, ipAddress + fileName);
            }
        } catch (Exception e) {
            log.error("uploadImg is error:", e);
        }
        return jsonObject;
    }


    public String getIpAddress(HttpServletRequest request) {
        String ip = null;

        // X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        // 有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        // 还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
