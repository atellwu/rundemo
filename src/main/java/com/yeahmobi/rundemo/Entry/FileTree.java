package com.yeahmobi.rundemo.Entry;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.yeahmobi.rundemo.project.JavaFileInfo;
import com.yeahmobi.rundemo.utils.CodeUtils;
/**
 * 构建前台文件树展示所需要的数据结构
 * @author Abel.cui
 *
 */
public class FileTree {
	public String name;//显示文件名
	public String type;//类型：item、folder
	public AdditionalParameters additionalParameters;//子目录
	
	public FileTree(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AdditionalParameters getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(AdditionalParameters additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public String toJsonString(){
		return JSONObject.toJSONString(this);
	}
	
	public static String getJsonFileList(File file, String app) throws IOException {
		List<FileTree> list = splice_JsonData(file.listFiles(), StringUtils.EMPTY, app);
		JSONObject object = new JSONObject();
		for(FileTree fileTree: list){
			object.put(fileTree.name, fileTree);
		}
		return object.toJSONString();
	}
	
	private static List<FileTree> splice_JsonData(File[] files, String prefix, String app) throws IOException{
		if(files == null || files.length <=0 ){
			return Collections.emptyList();
		}
		if(StringUtils.isNotBlank(prefix)){
			prefix = prefix +"_";
		}
		List<FileTree> mainlist = new ArrayList<FileTree>();
		for(int i = 0; i < files.length; i++){
			File file = files[i];
			FileTree fileTree = null;
			if (file.isDirectory()) {
				//给每个java文件编号，便于前端根据URL来定位文件
				fileTree = new FileTree(file.getName() + "<input type=hidden id=h_"+prefix+i+">", "folder");
				List<FileTree> list = splice_JsonData(file.listFiles(), prefix+i, app);
				AdditionalParameters additionalParameters = new AdditionalParameters(list);
				fileTree.setAdditionalParameters(additionalParameters);
			}else {
				//file.getName -> displayName
				JavaFileInfo javaFileInfo = CodeUtils.getJavaFileInfo(file, app);
				String name = "<a id="+prefix+i+" filePath="+file.getAbsolutePath()+">"+javaFileInfo.getDisplayName()+"</a>";
				fileTree = new FileTree(name, "item");
			}
			mainlist.add(fileTree);
		}
		return mainlist;
	}
}