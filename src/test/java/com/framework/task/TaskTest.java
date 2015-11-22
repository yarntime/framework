package com.framework.task;

import java.io.IOException;

import com.framework.resourcemanager.ResourceManagerTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TaskTest extends TestCase {

    public TaskTest() {
        super("TaskTest");
    }

    public static Test suite() {
        return new TestSuite(ResourceManagerTest.class);
    }

    public void testApp() throws IOException {
        try {
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
