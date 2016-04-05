/**
 *  
 * Function Description
 *
 * @author: zhangwei
 * @date:  Apr 1, 2016
 * @version: 1.0
 */
package com.framework.workflow;

import static org.junit.Assert.*;

import org.junit.Test;

public class WorkFlowTest {
    
    public WorkFlowChain generateWorkFlowChain() {
        WorkFlowChain chain = new WorkFlowChain();
        chain.getContext().put("counter", 0);
        return chain;
    }
    
    @Test
    public void workflowSuccessTest() {
        
        WorkFlowChain chain = generateWorkFlowChain();
        
        Integer successFlowCount = 5;
        for (int i = 0; i < successFlowCount; ++i) {
            MockSuccessFlow successFlow = new MockSuccessFlow();
            chain.add(successFlow);
        }
        
        chain.run();
        
        assertEquals(chain.getState(), WorkFlowChainState.ProcessDone);
        assertEquals(chain.getContext().get("counter"), 5);
    }

    @Test
    public void workflowFailTest_final() {
        WorkFlowChain chain = generateWorkFlowChain();
        
        Integer successFlowCount = 5;
        for (int i = 0; i < successFlowCount; ++i) {
            MockSuccessFlow successFlow = new MockSuccessFlow();
            chain.add(successFlow);
        }
        
        MockFailFlow failFlow = new MockFailFlow();
        chain.add(failFlow);
        
        chain.run();
        
        assertEquals(chain.getState(), WorkFlowChainState.ProcessFailed);
        
        chain.rollback();
        assertEquals(chain.getContext().get("counter"), 0);
    }
    
    @Test
    public void workflowFailTest_mid() {
        WorkFlowChain chain = generateWorkFlowChain();
        
        Integer successFlowCount = 5;
        for (int i = 0; i < successFlowCount; ++i) {
            MockSuccessFlow successFlow = new MockSuccessFlow();
            chain.add(successFlow);
            
            if (i == 2) {
                MockFailFlow failFlow = new MockFailFlow();
                chain.add(failFlow);
            }
        }
        
        chain.run();
        
        assertEquals(chain.getState(), WorkFlowChainState.ProcessFailed);
        
        chain.rollback();
        assertEquals(chain.getContext().get("counter"), 0);
    }
}
