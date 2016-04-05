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

public class MockSuccessFlow extends WorkFlow {

    @Override
    public void process(WorkFlowChainContext chainContext) throws WorkFlowException {
        Integer current = (Integer) chainContext.get("counter");
        current++;
        chainContext.put("counter", current);
    }

    @Override
    public void rollback(WorkFlowChainContext chainContext) throws WorkFlowException {
        Integer current = (Integer) chainContext.get("counter");
        current--;
        chainContext.put("counter", current);
    }

}
