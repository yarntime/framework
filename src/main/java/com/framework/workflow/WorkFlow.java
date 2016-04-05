/**
 * 
 * WorkFlow abstract class
 *
 * @author: zhangwei
 * @date: Mar 30, 2016
 * @version: 1.0
 */
package com.framework.workflow;

import com.framework.exception.WorkFlowException;

public abstract class WorkFlow {

    protected String name;

    // workflow context
    protected WorkFlowContext context = new WorkFlowContext();

    // process the flow
    protected abstract void process(WorkFlowChainContext chainContext) throws WorkFlowException;

    // rollback the flows in the chain
    protected abstract void rollback(WorkFlowChainContext chainContext) throws WorkFlowException;

    public WorkFlowContext getContext() {
        return context;
    }

    public void setContext(WorkFlowContext context) {
        this.context = context;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
