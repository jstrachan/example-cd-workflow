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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static demo.builds.StartProcessWorkItemHandler.logError;

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

        String triggerBuildURL = createTriggerBuildURL();
        try {
            URL url = new URL(triggerBuildURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream());
            out.write(json);
            out.close();

/*

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                System.out.println(decodedString);
            }
            in.close();
*/
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            System.out.println("Got response code: " + responseCode + " message: "+ responseMessage);
        } catch (IOException e) {
            logError("Failed to post to: " + triggerBuildURL + ". " + e, e);
        }
    }

    protected String createTriggerBuildURL() {
        String host = System.getenv("CDELIVERY_SERVICE_HOST");
        if (host == null) {
            host = "localhost";
        }
        String port = System.getenv("CDELIVERY_SERVICE_PORT");
        if (port == null) {
            port = "8787";
        } else {
            port = ":" + port;
        }
        return "http://" + host + port + "/triggerBuild";
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
