/*
 * Copyright 2020
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.clarin.webanno.codebook.ui.automation.generated.apiclient.auth;

import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.clarin.webanno.codebook.ui.automation.generated.apiclient.Pair;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-11-27T12:05:41.274Z[GMT]")
public class ApiKeyAuth
    implements Authentication
{
    private final String location;
    private final String paramName;

    private String apiKey;
    private String apiKeyPrefix;

    public ApiKeyAuth(String location, String paramName)
    {
        this.location = location;
        this.paramName = paramName;
    }

    public String getLocation()
    {
        return location;
    }

    public String getParamName()
    {
        return paramName;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getApiKeyPrefix()
    {
        return apiKeyPrefix;
    }

    public void setApiKeyPrefix(String apiKeyPrefix)
    {
        this.apiKeyPrefix = apiKeyPrefix;
    }

    @Override
    public void applyToParams(List<Pair> queryParams, Map<String, String> headerParams)
    {
        if (apiKey == null) {
            return;
        }
        String value;
        if (apiKeyPrefix != null) {
            value = apiKeyPrefix + " " + apiKey;
        }
        else {
            value = apiKey;
        }
        if ("query".equals(location)) {
            queryParams.add(new Pair(paramName, value));
        }
        else if ("header".equals(location)) {
            headerParams.put(paramName, value);
        }
    }
}
