package com.yeahmobi.rundemo.web;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.yeahmobi.rundemo.config.Config;
import com.yeahmobi.rundemo.utils.Constants;

@Controller
public class AdminController {
	private static final Logger LOG = LoggerFactory
			.getLogger(AdminController.class);
	
	@PostConstruct
	public void init() {
		try {
			Config.copyShellFile();
		} catch (IOException e) {
			LOG.info("copy shell files to target directory failed:"+e.getMessage());
		} catch (InterruptedException e) {
			LOG.info("give shell files permission failed:"+e.getMessage());
		}
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String create(String app, String gitUrl, String branch,
			String subdir, String mavenOpt, HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// (1)调用shell，下载git到本地，并且求出classpath
		if (StringUtils.isBlank(branch)) {
			branch = "master";
		}
		InputStream input = null;
		try {
			Process proc = Runtime.getRuntime().exec(
					new String[] { Config.shellDir + "down_git.sh", gitUrl,
							app, branch, mavenOpt, subdir });
			input = proc.getInputStream();
			LineIterator lineIterator = IOUtils
					.lineIterator(new InputStreamReader(input));
			while (lineIterator.hasNext()) {
				String line = lineIterator.next();
				LOG.info("down_git.sh output:" + line);
			}
			proc.waitFor();
			operateFiles(app, subdir);
			map.put("app", app);
			map.put("success", true);
		} catch (IOException e) {
			map.put("success", false);
			map.put("errorMsg", e.toString());
		} catch (InterruptedException e) {
			map.put("success", false);
			map.put("errorMsg", e.toString());
		} finally {
			IOUtils.closeQuietly(input);
		}
		Gson gson = new Gson();
		return gson.toJson(map);

		//return new RedirectView(request.getContextPath() + "/" + app);
	}

	private void operateFiles(String app, String subdir) throws IOException{
		if (!StringUtils.isBlank(subdir)) {
			subdir = "/" + subdir;
		} else {
			subdir = "";
		}
		// (2)Config.shellDir" + "/git_temp/{app}相关文件复制到appprojects/{app}
		// cp -rp Config.shellDir" + "/git_temp/{app}/subpath/src
		// Config.shellDir" + "/appprojects/{app}/src
		// cp -p Config.shellDir" + "/git_temp/{app}/subpath/pom.xml
		// Config.shellDir" + "/appprojects/{app}/pom.xml
		FileUtils.deleteDirectory(new File(Config.appprojectDir + app));
		this.copyFile2AppProject(app, subdir);
		// 删除gitTempDir/{app}
		FileUtils.deleteDirectory(new File(Config.gitTempDir + app + "/"));
	}

	private void copyFile2AppProject(String app, String subdir)
			throws IOException {
		File file = new File(Config.gitTempDir + app + subdir	+ "/");
		File[] files = file.listFiles(new CopyFileFilter());
		for (File f : files) {
			File newFile = new File(Config.appprojectDir + app + "/" + f.getName());
			if (f.isFile()) {
				FileUtils.copyFile(f, newFile);
			} else {
				FileUtils.copyDirectory(f, newFile);
			}
		}
	}
}

class CopyFileFilter implements FileFilter {
	public boolean accept(File file) {
		if (Constants.SRC.equals(file.getName())
				|| Constants.POM.equals(file.getName())
				|| Constants.CLASSPATH.equals(file.getName())) {
			return true;
		}
		return false;
	}
}