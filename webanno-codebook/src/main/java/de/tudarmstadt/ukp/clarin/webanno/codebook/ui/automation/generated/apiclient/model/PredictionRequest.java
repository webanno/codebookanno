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
package de.tudarmstadt.ukp.clarin.webanno.codebook.ui.automation.generated.apiclient.model;

import java.io.Serializable;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * PredictionRequest
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-12-18T10:42:36.085Z[GMT]")
public class PredictionRequest implements Serializable
{
    @SerializedName("doc")
    private DocumentDTO doc = null;

    @SerializedName("codebook")
    private CodebookDTO codebook = null;

    @SerializedName("mapping")
    private TagLabelMapping mapping = null;

    @SerializedName("model_version")
    private String modelVersion = "default";

    public PredictionRequest doc(DocumentDTO doc)
    {
        this.doc = doc;
        return this;
    }

    /**
     * Get doc
     * 
     * @return doc
     **/
    @Schema(required = true, description = "")
    public DocumentDTO getDoc()
    {
        return doc;
    }

    public void setDoc(DocumentDTO doc)
    {
        this.doc = doc;
    }

    public PredictionRequest codebook(CodebookDTO codebook)
    {
        this.codebook = codebook;
        return this;
    }

    /**
     * Get codebook
     * 
     * @return codebook
     **/
    @Schema(required = true, description = "")
    public CodebookDTO getCodebook()
    {
        return codebook;
    }

    public void setCodebook(CodebookDTO codebook)
    {
        this.codebook = codebook;
    }

    public PredictionRequest mapping(TagLabelMapping mapping)
    {
        this.mapping = mapping;
        return this;
    }

    /**
     * Get mapping
     * 
     * @return mapping
     **/
    @Schema(description = "")
    public TagLabelMapping getMapping()
    {
        return mapping;
    }

    public void setMapping(TagLabelMapping mapping)
    {
        this.mapping = mapping;
    }

    public PredictionRequest modelVersion(String modelVersion)
    {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Get modelVersion
     * 
     * @return modelVersion
     **/
    @Schema(description = "")
    public String getModelVersion()
    {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion)
    {
        this.modelVersion = modelVersion;
    }

    @Override
    public boolean equals(java.lang.Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PredictionRequest predictionRequest = (PredictionRequest) o;
        return Objects.equals(this.doc, predictionRequest.doc)
                && Objects.equals(this.codebook, predictionRequest.codebook)
                && Objects.equals(this.mapping, predictionRequest.mapping)
                && Objects.equals(this.modelVersion, predictionRequest.modelVersion);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(doc, codebook, mapping, modelVersion);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PredictionRequest {\n");

        sb.append("    doc: ").append(toIndentedString(doc)).append("\n");
        sb.append("    codebook: ").append(toIndentedString(codebook)).append("\n");
        sb.append("    mapping: ").append(toIndentedString(mapping)).append("\n");
        sb.append("    modelVersion: ").append(toIndentedString(modelVersion)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first
     * line).
     */
    private String toIndentedString(java.lang.Object o)
    {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
