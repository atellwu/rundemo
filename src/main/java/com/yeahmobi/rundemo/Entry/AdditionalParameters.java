package com.yeahmobi.rundemo.Entry;

import java.util.ArrayList;
import java.util.List;

public class AdditionalParameters {
	public List<FileTree> children = new ArrayList<FileTree>();

	public AdditionalParameters(List<FileTree> children) {
		this.children = children;
	}

	public List<FileTree> getChildren() {
		return children;
	}

	public void setChildren(List<FileTree> children) {
		this.children = children;
	}
}
