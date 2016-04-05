/**
 *  
 * Function Description
 *
 * @author: zhangwei
 * @date:  Apr 1, 2016
 * @version: 1.0
 */
package com.framework.workflow;

import com.framework.exception.WorkFlowException;

public class MockFailFlow extends WorkFlow {

    @Override
    public void process(WorkFlowChainContext chainContext) throws WorkFlowException {
        throw new WorkFlowException("failed");
    }

    @Override
    public void rollback(WorkFlowChainContext chainContext) throws WorkFlowException {
        
    }

}
