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
package de.tudarmstadt.ukp.clarin.webanno.codebook.ui.automation.generated.apiclient;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-11-27T12:05:41.274Z[GMT]")
public class Configuration
{
    private static ApiClient defaultApiClient = new ApiClient();

    /**
     * Get the default API client, which would be used when creating API instances without providing
     * an API client.
     *
     * @return Default API client
     */
    public static ApiClient getDefaultApiClient()
    {
        return defaultApiClient;
    }

    /**
     * Set the default API client, which would be used when creating API instances without providing
     * an API client.
     *
     * @param apiClient
     *            API client
     */
    public static void setDefaultApiClient(ApiClient apiClient)
    {
        defaultApiClient = apiClient;
    }
}
