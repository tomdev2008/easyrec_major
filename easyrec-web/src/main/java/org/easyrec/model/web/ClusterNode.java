/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easyrec.model.web;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;

/**
 *
 * @author Stephan
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ClusterNode {
    
    String data;
    NodeAttributes attr;
    String description;
    String rel;
    List<ClusterNode> children;

    public ClusterNode() {
    }
    
    public ClusterNode(String data, NodeAttributes attr, String description) {
        this.data = data;
        this.attr = attr;
        this.description = description;
    }
    
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public NodeAttributes getAttr() {
        return attr;
    }

    public void setAttr(NodeAttributes attr) {
        this.attr = attr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ClusterNode> getChildren() {
        return children;
    }

    public void setChildren(List<ClusterNode> children) {
        this.children = children;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }
    
    
    
    
}
