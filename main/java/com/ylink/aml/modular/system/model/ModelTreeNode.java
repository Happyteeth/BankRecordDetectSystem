package com.ylink.aml.modular.system.model;

public class ModelTreeNode extends TreeNode {
    public ModelTreeNode(String id, String parentId, String title) {
        super(id, parentId, title);
    }

    public ModelTreeNode(String id,String parentId, String title, long resultCount,long checkCount, String status,String proportion) {
        super(id,parentId, title, resultCount, checkCount,status,proportion);
    }
    
}