/**
 * 
 * WorkFlow context, each workflow has its own workflow context information.
 * the context only contains the information required by this workflow.
 *
 * @author: zhangwei
 * @date: Mar 30, 2016
 * @version: 1.0
 */
package com.framework.workflow;

import java.util.HashMap;
import java.util.Map;

public class WorkFlowContext {

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
