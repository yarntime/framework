/**
 * 
 * one workflowchain corresponding to one task, one workflowchain may contains one or more workflow.
 * if one workflow fails, rollback all workflows that have been processed.
 *
 * @author: zhangwei
 * @date: Mar 30, 2016
 * @version: 1.0
 */
package com.framework.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.framework.exception.WorkFlowException;

public class WorkFlowChain {

    private static Logger LOGGER = Logger.getLogger(WorkFlowChain.class);

    private String name;

    private String owner;

    private String uuid;

    private WorkFlowChainState state;

    private int position;

    private List<WorkFlow> flows = new ArrayList<WorkFlow>();

    private String cause;
    
    private WorkFlowChainContext context = new WorkFlowChainContext();

    public WorkFlowChain(String name) {
        this.name = name;
    }

    public WorkFlowChain() {
        this.name = "skyform";
    }

    public WorkFlowChain add(WorkFlow flow) {
        flows.add(flow);
        return this;
    }

    // build workflow chain, generate uuid for the chain, it may used as response ticket
    public WorkFlowChain build() {
        if (this.owner == null) {
            this.owner = "skyform";
        }

        if (flows.isEmpty()) {
            throw new IllegalArgumentException(
                    "WorkFlowChain cannot be built without adding any WorkFlow in it");
        }

        StringBuilder sb = new StringBuilder(getName());
        sb.append(getOwner());
        for (WorkFlow f : flows) {
            String name = f.getName();
            name = name == null ? f.getClass().getCanonicalName() : name;
            sb.append(name);
        }

        uuid = UUID.nameUUIDFromBytes(sb.toString().getBytes()).toString().replace("-", "");
        this.state = WorkFlowChainState.Waiting;
        return this;
    }

    public WorkFlowChain run() {
        if (this.uuid == null) {
            this.build();
        }

        this.state = WorkFlowChainState.Processing;

        for (int i = 0; i < flows.size(); i++) {
            this.position = i;
            WorkFlow flow = flows.get(i);
            Boolean result = processFlow(flow);
            if (!result) {
                this.state = WorkFlowChainState.ProcessFailed;
                LOGGER.error("failed to process workflow chain " + this.getName());
                return this;
            }
        }

        this.state = WorkFlowChainState.ProcessDone;
        return this;
    }

    public WorkFlowChain rollback() {

        if (this.uuid == null) {
            this.state = WorkFlowChainState.RollbackDone;
        }

        for (int i = 0; i < position; i++) {
            rollbackFlow(flows.get(i));
        }
        this.state = WorkFlowChainState.RollbackDone;
        return this;
    }

    private Boolean processFlow(WorkFlow flow) {
        try {
            flow.process(this.context);
            LOGGER.debug(String.format("Successfully processed workflow[%s] in chain[%s]",
                    flow.getName(), getName()));
            return true;
        } catch (WorkFlowException e) {
            this.cause = String.format("workflow[%s] in chain[%s] failed because %s",
                    flow.getName(), getName(), e.getMessage());
            LOGGER.debug(this.cause);
            return false;
        } catch (Throwable t) {
            this.cause = String.format(
                    "workflow[%s] in chain[%s] failed because %s",
                    flow.getName(), getName(), t.getMessage());
            LOGGER.debug(this.cause, t);
            return false;
        }
    }

    protected void rollbackFlow(WorkFlow flow) {
        try {
            flow.rollback(this.context);
            LOGGER.debug(String.format("Successfully rolled back workflow[%s] in chain[%s]",
                    flow.getName(), getName()));
        } catch (Throwable t) {
            LOGGER.warn(String.format(
                    "Unhandled exception happend while rolling back workflow[%s] in chain[%s]",
                    flow.getName(), getName()), t);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public WorkFlowChainState getState() {
        return state;
    }

    public void setState(WorkFlowChainState state) {
        this.state = state;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public WorkFlowChainContext getContext() {
        return context;
    }
}
