package com.dianping.rundemo.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.dianping.rundemo.config.Config;

@Controller
public class AdminController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @RequestMapping(value = "/create")
    public RedirectView create(String app, String gitUrl, String branch, String subdir, String mavenOpt,
                               HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
        //(1)调用shell，下载git到本地，并且求出classpath
        if (StringUtils.isBlank(branch)) {
            branch = "master";
        }
        InputStream input = null;
        try {
            Process proc = Runtime.getRuntime().exec(
                    new String[] { Config.shellDir + "/down_git.sh", gitUrl, app, branch, mavenOpt, subdir });
            input = proc.getInputStream();
            LineIterator lineIterator = IOUtils.lineIterator(new InputStreamReader(input));
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                LOG.info("down_git.sh output:" + line);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
        //(2)Config.shellDir" + "/git_temp/{app}相关文件复制到appprojects/{app}
        //cp -rp Config.shellDir" + "/git_temp/{app}/subpath/src Config.shellDir" + "/appprojects/{app}/src
        //cp -p Config.shellDir" + "/git_temp/{app}/subpath/pom.xml Config.shellDir" + "/appprojects/{app}/pom.xml
        if (!StringUtils.isBlank(subdir)) {
            subdir = "/" + subdir;
        } else {
            subdir = "";
        }
        FileUtils.deleteDirectory(new File(Config.shellDir + "/appprojects/" + app));
        File srcDir1 = new File(Config.shellDir + "/git_temp/" + app + subdir + "/src");
        File destDir1 = new File(Config.shellDir + "/appprojects/" + app + "/src");
        FileUtils.copyDirectory(srcDir1, destDir1);
        File srcDir2 = new File(Config.shellDir + "/git_temp/" + app + subdir + "/pom.xml");
        File destDir2 = new File(Config.shellDir + "/appprojects/" + app + "/pom.xml");
        FileUtils.copyFile(srcDir2, destDir2);
        File srcDir3 = new File(Config.shellDir + "/git_temp/" + app + subdir + "/classpath");
        File destDir3 = new File(Config.shellDir + "/appprojects/" + app + "/classpath");
        FileUtils.copyFile(srcDir3, destDir3);
        //删除Config.shellDir" + "/git_temp/{app}
        FileUtils.deleteDirectory(new File(Config.shellDir + "/git_temp/" + app + "/"));

        return new RedirectView(request.getContextPath() + "/" + app);
    }
}
