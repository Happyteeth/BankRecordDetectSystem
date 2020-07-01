package com.ylink.aml.modular.system.model;

import com.ylink.aml.modular.system.dto.RuleRunDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class TreeNode {
	protected String id;
	protected String parentId;
	private String title;
	private  String type;
	private  Integer  level;
	private long resultCount;
	private long checkCount;
	private String status;

	/**
	 *  违规占比
	 */
	private String proportion;
	private  boolean  forceShow =false;  //是否强制显示

	protected List<TreeNode> children = new ArrayList<TreeNode>();

	public void add(TreeNode node) {
		children.add(node);
	}

	public TreeNode(String id,String parentId,String title){
		this.id =id;
		this.parentId =parentId;
		this.title =title;
	}
	public TreeNode(String id,String parentId,String title,long resultCount,long checkCount,String status,String proportion){
		this.id =id;
		this.parentId =parentId;
		this.title =title;
		this.resultCount = resultCount;
		this.checkCount = checkCount;
		this.status =status;
		this.proportion=proportion;
	}
	public TreeNode( ){
	}
}
