package com.yeahmobi.rundemo.Entry;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
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
	
	public static String getJsonFileList(File file) {
		List<FileTree> list = splice_JsonData(file.listFiles(), "");
		JSONObject object = new JSONObject();
		for(FileTree fileTree: list){
			object.put(fileTree.name, fileTree);
		}
		return object.toJSONString();
	}
	
	private static List<FileTree> spliceJsonData(File[] files,int index){
		List<FileTree> mainlist = new ArrayList<FileTree>();
		for(File file : files){
			FileTree fileTree = null;
			if (file.isDirectory()) {
				//默认每个目录下有1W个文件，给每个java文件编号，便于前端根据URL来定位文件
				fileTree = new FileTree(file.getName() + "<input type=hidden id=h_"+(index*10000)+">", "folder");
				List<FileTree> list = spliceJsonData(file.listFiles(), (index++)*10000);
				AdditionalParameters additionalParameters = new AdditionalParameters(list);
				fileTree.setAdditionalParameters(additionalParameters);
			}else {
				String name = "<a id=item_"+(index)+" href=javascript:rundemo_app.changeJavaHash("+(index++)+"); filePath="+file.getAbsolutePath()+">"+file.getName()+"</a>";
				fileTree = new FileTree(name, "item");
			}
			mainlist.add(fileTree);
		}
		return mainlist;
	}
	
	private static List<FileTree> splice_JsonData(File[] files, String prefix){
		if(StringUtils.isNotBlank(prefix)){
			prefix = prefix +"_";
		}
		List<FileTree> mainlist = new ArrayList<FileTree>();
		for(int i = 0; i < files.length; i++){
			File file = files[i];
			FileTree fileTree = null;
			if (file.isDirectory()) {
				//默认每个目录下有1W个文件，给每个java文件编号，便于前端根据URL来定位文件
				fileTree = new FileTree(file.getName() + "<input type=hidden id=h_"+prefix+i+">", "folder");
				List<FileTree> list = splice_JsonData(file.listFiles(), prefix+i);
				AdditionalParameters additionalParameters = new AdditionalParameters(list);
				fileTree.setAdditionalParameters(additionalParameters);
			}else {
				String name = "<a id="+prefix+i+" filePath="+file.getAbsolutePath()+">"+file.getName()+"</a>";
				fileTree = new FileTree(name, "item");
			}
			mainlist.add(fileTree);
		}
		return mainlist;
	}
}