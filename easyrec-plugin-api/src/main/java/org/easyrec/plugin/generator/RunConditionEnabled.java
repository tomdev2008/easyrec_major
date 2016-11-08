/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easyrec.plugin.generator;

import java.util.Date;

/**
 *
 * @author Stephan Zavrel
 */
public interface RunConditionEnabled {
    
    public boolean evaluateRuncondition(Date lastRun);
    
}
