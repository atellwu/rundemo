package com.yeahmobi.rundemo.web;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.yeahmobi.rundemo.utils.Constants;

@Controller
public class AdminController {
	private static final Logger LOG = LoggerFactory
			.getLogger(AdminController.class);
	
	private String shellDir;
	private String appprojectDir;
	private String gitTempDir;
	
	@Value("#{confProperties['demoDir']}")
	private String demoDir;

	@PostConstruct
	public void init() {
		this.appprojectDir = this.demoDir + "appprojects/";
		this.shellDir = this.demoDir + "shell/";
		this.gitTempDir = this.demoDir + "git_temp/";
	}

	@RequestMapping(value = "/create")
	public RedirectView create(String app, String gitUrl, String branch,
			String subdir, String mavenOpt, HttpServletRequest request,
			HttpServletResponse response) throws FileNotFoundException,
			IOException, InterruptedException {
		// (1)调用shell，下载git到本地，并且求出classpath
		if (StringUtils.isBlank(branch)) {
			branch = "master";
		}
		InputStream input = null;
		try {
			Process proc = Runtime.getRuntime().exec(
					new String[] { this.shellDir + "down_git.sh", gitUrl,
							app, branch, mavenOpt, subdir });
			input = proc.getInputStream();
			LineIterator lineIterator = IOUtils
					.lineIterator(new InputStreamReader(input));
			while (lineIterator.hasNext()) {
				String line = lineIterator.next();
				LOG.info("down_git.sh output:" + line);
			}
			proc.waitFor();
		} finally {
			IOUtils.closeQuietly(input);
		}
		// (2)Config.shellDir" + "/git_temp/{app}相关文件复制到appprojects/{app}
		// cp -rp Config.shellDir" + "/git_temp/{app}/subpath/src
		// Config.shellDir" + "/appprojects/{app}/src
		// cp -p Config.shellDir" + "/git_temp/{app}/subpath/pom.xml
		// Config.shellDir" + "/appprojects/{app}/pom.xml
		if (!StringUtils.isBlank(subdir)) {
			subdir = "/" + subdir;
		} else {
			subdir = "";
		}
		FileUtils.deleteDirectory(new File(appprojectDir + app));
		this.copyFile2AppProject(app, subdir);
		// 删除gitTempDir/{app}
		FileUtils.deleteDirectory(new File(this.gitTempDir + app + "/"));

		return new RedirectView(request.getContextPath() + "/" + app);
	}

	private void copyFile2AppProject(String app, String subdir)
			throws IOException {
		File file = new File(this.gitTempDir + app + subdir	+ "/");
		File[] files = file.listFiles(new CopyFileFilter());
		for (File f : files) {
			File newFile = new File(this.appprojectDir + app + "/" + f.getName());
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