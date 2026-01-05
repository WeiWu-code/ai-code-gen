package xd.ww.wwaicodegen.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     * 将某文件夹的文件打包下载
     * @param projectPath 项目目录
     * @param downloadFileName 待下载的文件名字（显示在浏览器中）
     * @param response 响应
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
