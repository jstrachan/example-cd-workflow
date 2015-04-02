/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.builds;

import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Starts a new workflow process
 */
public class StartProcessWorkItemHandler implements WorkItemHandler {
    private KieSession ksession;

    public StartProcessWorkItemHandler(KieSession ksession) {
        this.ksession = ksession;
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        Map<String, Object> parameters = workItem.getParameters();
        String startNodeName = (String) parameters.get("startSignalName");
        if (isEmpty(startNodeName)) {
            System.out.println("No startSignalName parameter!");
            Set<Map.Entry<String, Object>> entries = parameters.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                System.out.println(" Parameter " + entry.getKey() + " = " + entry.getValue());
            }
            long processInstanceId = workItem.getProcessInstanceId();
            ProcessInstance processInstance = ksession.getProcessInstance(processInstanceId);
            if (processInstance instanceof WorkflowProcessInstance) {
                WorkflowProcessInstance instance = (WorkflowProcessInstance) processInstance;
                Object value = instance.getVariable("startSignalName");
                if (value != null) {
                    System.out.println("==== ooh found variable on process");
                    startNodeName = value.toString();
                }
            }
        }
        if (isEmpty(startNodeName)) {
            return;
        }

        logInfo("Looking for processes with a startSignalName " + startNodeName);
        Map<String, Object> inputParameters = new HashMap<String, Object>();
        // TODO
        // populate input parameters
        Collection<Process> processes = ksession.getKieBase().getProcesses();
        int startCount = 0;
        for (Process process : processes) {
            if (process instanceof WorkflowProcess) {
                WorkflowProcess workflowProcess = (WorkflowProcess) process;
                Node[] nodes = workflowProcess.getNodes();
                if (nodes != null) {
                    for (Node node : nodes) {
                        String name = node.getName();
                        if (Objects.equals(startNodeName, name)) {
                            String processId = process.getId();
                            logInfo("Starting process " + processId + " with parameters: " + inputParameters);
                            startCount++;
                            try {
                                ksession.startProcess(processId, inputParameters);
                            } catch (Exception e) {
                                logError("Could not start process " + processId + " with parameters " + inputParameters + ". Reason: " + e, e);
                            }
                        }
                    }
                }
            }
        }
        if (startCount == 0) {
            logInfo("No business process starts with signal of name: " + startNodeName);
        }
    }

    protected static boolean isEmpty(String startNodeName) {
        return startNodeName == null || startNodeName.length() == 0;
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }

    public static void logInfo(String message) {
        System.out.println(message);
    }

    public static void logError(String message, Exception e) {
        System.out.println("ERROR: "+ message + ". " + e);
        e.printStackTrace();
    }
}