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

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class BuildWorkItemHandler implements WorkItemHandler {
    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        Map<String, Object> parameters = workItem.getParameters();
        Map<String, Object> allParameters = new HashMap<String, Object>(parameters);
        allParameters.put("processInstanceId", workItem.getProcessInstanceId());
        allParameters.put("workItemId", workItem.getId());


        // lets turn it into JSON
        StringBuilder buffer = new StringBuilder("{");
        int count = 0;
        Set<Map.Entry<String, Object>> entries = allParameters.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
                if (count++ > 0) {
                    buffer.append(", ");
                }
                encodeJson(buffer, key);
                buffer.append(": ");
                encodeJson(buffer, value);
            }
        }
        buffer.append("}");

        String json = buffer.toString();

        System.out.println("About to post JSON: " + json);
    }

    protected void encodeJson(StringBuilder buffer, Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            buffer.append(value.toString());
        } else {
            // lets assume a string
            buffer.append('"');
            buffer.append(value.toString());
            buffer.append('"');
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }
}
