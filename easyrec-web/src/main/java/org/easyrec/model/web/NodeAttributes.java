/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easyrec.model.web;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 *
 * @author Stephan
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class NodeAttributes {
    
    String id;
    String title;

    public NodeAttributes() {
    }

    public NodeAttributes(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    
    
}
