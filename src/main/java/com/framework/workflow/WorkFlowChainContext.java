/**
 *  
 * WorkFlowChain context, every workflow chain contains one workflowchain context,
 * the context contains the information required by all workflows in this workflow chain.
 * 
 * @author: zhangwei
 * @date:  Apr 1, 2016
 * @version: 1.0
 */
package com.framework.workflow;

import java.util.HashMap;
import java.util.Map;

public class WorkFlowChainContext {

    private Map<String, Object> context = new HashMap<String, Object>();

    public void put(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public void remove(String key) {
        context.remove(key);
    }
}
