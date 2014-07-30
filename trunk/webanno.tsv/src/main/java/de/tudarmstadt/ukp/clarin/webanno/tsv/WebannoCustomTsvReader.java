/*******************************************************************************
 * Copyright 2012
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
 ******************************************************************************/
package de.tudarmstadt.ukp.clarin.webanno.tsv;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.CasUtil.getType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * This class reads a WebAnno compatible TSV files and create annotations from the information
 * provided. The very beginning of the file, the header, mentions the existing annotation layers
 * with their feature structure information Annotation layers are separated by # character and
 * features by | character. If the layer is a relation annotation, it includes the string
 * AttachToType=... where the attach type is expressed.There is no Chain TSV reader Writer yet.
 *
 * @author Seid Muhie Yimam
 *
 */
public class WebannoCustomTsvReader
    extends JCasResourceCollectionReader_ImplBase
{

    private String fileName;

    public void convertToCas(JCas aJCas, InputStream aIs, String aEncoding)
        throws IOException

    {
        StringBuilder text = new StringBuilder();
        DocumentMetaData documentMetadata = DocumentMetaData.get(aJCas);
        fileName = documentMetadata.getDocumentTitle();
        setAnnotations(aJCas, aIs, aEncoding, text);
        aJCas.setDocumentText(text.toString());
    }

    /**
     * Iterate through lines and create span annotations accordingly. For multiple span annotation,
     * based on the position of the annotation in the line, update only the end position of the
     * annotation
     */
    private void setAnnotations(JCas aJcas, InputStream aIs, String aEncoding, StringBuilder text)
        throws IOException
    {

        // getting header information
        LineIterator lineIterator = IOUtils.lineIterator(aIs, aEncoding);
        int columns = 1;// token number + token columns (minimum required)
        int tokenStart = 0, sentenceStart = 0;
        Map<Type, Set<Feature>> spanLayers = new LinkedHashMap<Type, Set<Feature>>();
        Map<Type, Type> relationayers = new LinkedHashMap<Type, Type>();

        // an annotation for every feature in a layer
        Map<Type, Map<Integer, AnnotationFS>> annotations = new LinkedHashMap<Type, Map<Integer, AnnotationFS>>();

        // store if this is a Begin/Intermediate/End of an annotation
        Map<Type, Map<Integer, String>> beginEndAnno = new LinkedHashMap<Type, Map<Integer, String>>();

        // Store annotations of tokens so that it can be used later for relation annotations
        Map<Type, Map<String, List<AnnotationFS>>> tokenAnnotations = new LinkedHashMap<Type, Map<String, List<AnnotationFS>>>();

        // store target token ids used for a relation
        Map<Type, Map<String, List<String>>> relationTargets = new LinkedHashMap<Type, Map<String, List<String>>>();

        while (lineIterator.hasNext()) {
            String line = lineIterator.next().trim();
            if (line.trim().equals("") && sentenceStart == tokenStart) {
                continue;
            }
            if (line.trim().equals("")) {
                text.replace(tokenStart - 1, tokenStart, "");
                tokenStart = tokenStart - 1;
                Sentence sentence = new Sentence(aJcas, sentenceStart, tokenStart);
                sentence.addToIndexes();
                tokenStart++;
                sentenceStart = tokenStart;
                text.append("\n");
                continue;
            }
            // sentence
            if (line.startsWith("#text=")) {
                continue;
            }
            if (line.startsWith("#id=")) {
                continue;// it is a comment line
            }
            if (line.startsWith("#")) {
                columns = getLayerAndFeature(aJcas, columns, spanLayers, relationayers, line);
                continue;
            }
            // some times, the sentence in #text= might have a new line which break this reader,
            // so skip such lines
            if(!Character.isDigit(line.split(" ")[0].charAt(0))){
                continue;
            }

            // If we are still unlucky, the line starts with a number from the sentence but not
            // a token number, check if it didn't in the format NUM-NUM
            if(!Character.isDigit(line.split("-")[1].charAt(0))){
                continue;
            }

            int count = StringUtils.countMatches(line, "\t");

            if (columns != count) {
                throw new IOException(fileName + " This is not a valid TSV File. check this line: "
                        + line);
            }

            // adding tokens and sentence
            StringTokenizer lineTk = new StringTokenizer(line, "\t");
            String tokenNumberColumn = lineTk.nextToken();
            String tokenColumn = lineTk.nextToken();
            Token token = new Token(aJcas, tokenStart, tokenStart + tokenColumn.length());
            token.addToIndexes();

            // adding the annotations
            createSpanAnnotation(aJcas, tokenStart, spanLayers, relationayers, annotations,
                    beginEndAnno, tokenAnnotations, relationTargets, lineTk, tokenColumn,
                    tokenNumberColumn);

            tokenStart = tokenStart + tokenColumn.length() + 1;
            text.append(tokenColumn + " ");
        }
        if (tokenStart > sentenceStart) {
            Sentence sentence = new Sentence(aJcas, sentenceStart, tokenStart);
            sentence.addToIndexes();
            text.append("\n");
        }

        createRelationLayer(aJcas, relationayers, tokenAnnotations, relationTargets);
    }

    private int getLayerAndFeature(JCas aJcas, int columns, Map<Type, Set<Feature>> spanLayers,
            Map<Type, Type> relationayers, String line)
        throws IOException
    {
        StringTokenizer headerTk = new StringTokenizer(line, "#");
        while (headerTk.hasMoreTokens()) {
            String layerNames = headerTk.nextToken().trim();
            StringTokenizer layerTk = new StringTokenizer(layerNames, "|");

            Set<Feature> features = new LinkedHashSet<Feature>();
            String layerName = layerTk.nextToken().trim();

            Iterator<Type> types = aJcas.getTypeSystem().getTypeIterator();
            boolean layerExists = false;
            while (types.hasNext()) {

                if (types.next().getName().equals(layerName)) {
                    layerExists = true;
                    break;
                }
            }
            if (!layerExists) {
                throw new IOException(fileName + " This is not a valid TSV File. The layer "
                        + layerName + " is not created in the project.");
            }
            Type layer = CasUtil.getType(aJcas.getCas(), layerName);

            while (layerTk.hasMoreTokens()) {
                String ft = layerTk.nextToken().trim();
                if (ft.startsWith("AttachTo=")) {
                    Type attachLayer = CasUtil.getType(aJcas.getCas(), ft.substring(9));
                    relationayers.put(layer, attachLayer);
                    columns++;
                    continue;
                }
                Feature feature = layer.getFeatureByBaseName(ft);
                if (feature == null) {
                    throw new IOException(fileName + " This is not a valid TSV File. The feature "
                            + ft + " is not created for the layer " + layerName);
                }
                features.add(feature);
                columns++;
            }
            spanLayers.put(layer, features);
        }
        return columns;
    }

    /**
     * Creates a relation layer. For every token, store the governor positions and the dependent
     * annotation
     */

    private void createRelationLayer(JCas aJcas, Map<Type, Type> relationayers,
            Map<Type, Map<String, List<AnnotationFS>>> tokenAnnotations,
            Map<Type, Map<String, List<String>>> relationTargets)
    {

        for (Type layer : relationayers.keySet()) {

            Feature dependentFeature = layer.getFeatureByBaseName("Dependent");
            Feature governorFeature = layer.getFeatureByBaseName("Governor");

            Map<String, List<String>> tokenIdMaps = relationTargets.get(layer);
            Map<String, List<AnnotationFS>> tokenAnnos = tokenAnnotations.get(relationayers
                    .get(layer));
            Map<String, List<AnnotationFS>> relationAnnos = tokenAnnotations.get(layer);
            for (String dependnetId : tokenIdMaps.keySet()) {
                int i = 0;
                for (String governorId : tokenIdMaps.get(dependnetId)) {

                    AnnotationFS relationAnno = relationAnnos.get(dependnetId).get(i);
                    AnnotationFS dependentAnno = tokenAnnos.get(dependnetId).get(0);
                    AnnotationFS governorAnno = tokenAnnos.get(governorId).get(0);

                    if (layer.getName().equals(Dependency.class.getName())) {
                        Type tokenType = getType(aJcas.getCas(), Token.class.getName());
                        Feature attachFeature = tokenType.getFeatureByBaseName("pos");
                        AnnotationFS posDependentAnno = dependentAnno;
                        dependentAnno = CasUtil.selectCovered(aJcas.getCas(), tokenType,
                                dependentAnno.getBegin(), dependentAnno.getEnd()).get(0);
                        dependentAnno.setFeatureValue(attachFeature, posDependentAnno);

                        AnnotationFS posGovernorAnno = governorAnno;
                        governorAnno = CasUtil.selectCovered(aJcas.getCas(), tokenType,
                                governorAnno.getBegin(), governorAnno.getEnd()).get(0);
                        governorAnno.setFeatureValue(attachFeature, posGovernorAnno);
                    }
                    // update begin/end of relation annotation
                    if (dependentAnno.getEnd() <= governorAnno.getEnd()) {
                        ((Annotation) relationAnno).setBegin(dependentAnno.getBegin());
                        ((Annotation) relationAnno).setEnd(governorAnno.getEnd());
                    }
                    else {
                        ((Annotation) relationAnno).setBegin(governorAnno.getBegin());
                        ((Annotation) relationAnno).setEnd(dependentAnno.getEnd());
                    }

                    relationAnno.setFeatureValue(dependentFeature, dependentAnno);
                    relationAnno.setFeatureValue(governorFeature, governorAnno);
                    i++;
                }

            }
        }
    }

    private void createSpanAnnotation(JCas aJcas, int aTokenStart, Map<Type, Set<Feature>> aLayers,
            Map<Type, Type> aRelationayers, Map<Type, Map<Integer, AnnotationFS>> aAnnotations,
            Map<Type, Map<Integer, String>> aBeginEndAnno,
            Map<Type, Map<String, List<AnnotationFS>>> aTokenAnnotations,
            Map<Type, Map<String, List<String>>> aRelationTargets, StringTokenizer lineTk,
            String aToken, String aTokenNumberColumn)
    {
        for (Type layer : aLayers.keySet()) {
            int lastIndex = 1;
            for (Feature feature : aLayers.get(layer)) {
                int index = 1;
                String multipleAnnotations = lineTk.nextToken();
                String relationTargetNumbers = null;
                if (aRelationayers.containsKey(layer)) {
                    relationTargetNumbers = lineTk.nextToken();
                }
                int i = 0;
                String[] relationTargets = null;
                if (relationTargetNumbers != null) {
                    relationTargets = relationTargetNumbers.split("\\|");
                }
                for (String annotation : multipleAnnotations.split("\\|")) {
                    // If annotation is not on multpile spans
                    if (!(annotation.startsWith("B-") || annotation.startsWith("I-") || annotation
                            .startsWith("O-")) && !(annotation.equals("_")||annotation.equals("O"))) {
                        AnnotationFS newAnnotation = aJcas.getCas().createAnnotation(layer,
                                aTokenStart, aTokenStart + aToken.length());
                        newAnnotation.setFeatureValueFromString(feature, annotation);
                        aJcas.addFsToIndexes(newAnnotation);

                        if (aRelationayers.containsKey(layer)) {
                            Map<String, List<String>> targets = aRelationTargets.get(layer);
                            if (targets == null) {
                                List<String> governors = new ArrayList<String>();
                                governors.add(relationTargets[i]);
                                targets = new HashMap<String, List<String>>();
                                targets.put(aTokenNumberColumn, governors);
                                i++;
                                aRelationTargets.put(layer, targets);
                            }
                            else {
                                List<String> governors = targets.get(aTokenNumberColumn);
                                if (governors == null) {
                                    governors = new ArrayList<String>();
                                }
                                governors.add(relationTargets[i]);
                                targets.put(aTokenNumberColumn, governors);
                                i++;
                                aRelationTargets.put(layer, targets);
                            }
                        }

                        Map<String, List<AnnotationFS>> tokenAnnotations = aTokenAnnotations
                                .get(layer);
                        if (tokenAnnotations == null) {
                            tokenAnnotations = new HashMap<String, List<AnnotationFS>>();
                        }
                        List<AnnotationFS> relAnnos = tokenAnnotations.get(aTokenNumberColumn);
                        if (relAnnos == null) {
                            relAnnos = new ArrayList<AnnotationFS>();
                        }
                        relAnnos.add(newAnnotation);
                        tokenAnnotations.put(aTokenNumberColumn, relAnnos);
                        aTokenAnnotations.put(layer, tokenAnnotations);
                    }
                    // for annotations such as B_LOC|B-_|I_PER and the like
                    // O-_ is a position marker
                    else if (annotation.equals("O-_") || annotation.equals("B-_")
                            || annotation.equals("I-_")) {
                        index++;
                    }
                    else if (annotation.startsWith("B-")) {
                        boolean isNewAnnotation = true;
                        Map<Integer, AnnotationFS> indexedAnnos = aAnnotations.get(layer);
                        Map<Integer, String> indexedBeginEndAnnos = aBeginEndAnno.get(layer);
                        AnnotationFS newAnnotation;

                        if (indexedAnnos == null) {
                            newAnnotation = aJcas.getCas().createAnnotation(layer, aTokenStart,
                                    aTokenStart + aToken.length());
                            indexedAnnos = new LinkedHashMap<Integer, AnnotationFS>();
                            indexedBeginEndAnnos = new LinkedHashMap<Integer, String>();
                        }
                        else if (indexedAnnos.get(index) == null) {
                            newAnnotation = aJcas.getCas().createAnnotation(layer, aTokenStart,
                                    aTokenStart + aToken.length());
                        }
                        else if (indexedAnnos.get(index) != null
                                && indexedBeginEndAnnos.get(index).equals("E-")) {
                            newAnnotation = aJcas.getCas().createAnnotation(layer, aTokenStart,
                                    aTokenStart + aToken.length());
                        }
                        // B-LOC I-LOC B-LOC - the last B-LOC is a new annotation
                        else if (indexedBeginEndAnnos.get(index).equals("I-")) {
                            newAnnotation = aJcas.getCas().createAnnotation(layer, aTokenStart,
                                    aTokenStart + aToken.length());
                        }
                        else {
                            newAnnotation = indexedAnnos.get(index);
                            isNewAnnotation = false;
                        }
                        // remove prefixes such as B-/I- before creating the annotation
                        newAnnotation.setFeatureValueFromString(feature, (annotation.substring(2)));
                        aJcas.addFsToIndexes(newAnnotation);
                        indexedAnnos.put(index, newAnnotation);
                        indexedBeginEndAnnos.put(index, "B-");
                        aAnnotations.put(layer, indexedAnnos);

                        if (aRelationayers.containsKey(layer)) {
                            Map<String, List<String>> targets = aRelationTargets.get(layer);
                            if (targets == null) {
                                List<String> governors = new ArrayList<String>();
                                governors.add(relationTargets[i]);
                                targets = new HashMap<String, List<String>>();
                                targets.put(aTokenNumberColumn, governors);
                                i++;
                                aRelationTargets.put(layer, targets);
                            }
                            else {
                                List<String> governors = targets.get(aTokenNumberColumn);
                                if (governors == null) {
                                    governors = new ArrayList<String>();
                                }
                                governors.add(relationTargets[i]);
                                targets.put(aTokenNumberColumn, governors);
                                i++;
                                aRelationTargets.put(layer, targets);
                            }
                        }

                        Map<String, List<AnnotationFS>> tokenAnnotations = aTokenAnnotations
                                .get(layer);
                        if (isNewAnnotation) {
                            if (tokenAnnotations == null) {
                                tokenAnnotations = new HashMap<String, List<AnnotationFS>>();
                            }
                            List<AnnotationFS> relAnnos = tokenAnnotations.get(aTokenNumberColumn);
                            if (relAnnos == null) {
                                relAnnos = new ArrayList<AnnotationFS>();
                            }
                            relAnnos.add(newAnnotation);
                            tokenAnnotations.put(aTokenNumberColumn, relAnnos);
                            aTokenAnnotations.put(layer, tokenAnnotations);
                        }

                        aBeginEndAnno.put(layer, indexedBeginEndAnnos);
                        index++;
                    }

                    else if (annotation.startsWith("I-")) {
                        // beginEndAnnotation.put(layer, "I-");

                        Map<Integer, String> indexedBeginEndAnnos = aBeginEndAnno.get(layer);
                        indexedBeginEndAnnos.put(index, "I-");
                        aBeginEndAnno.put(layer, indexedBeginEndAnnos);

                        Map<Integer, AnnotationFS> indexedAnnos = aAnnotations.get(layer);
                        AnnotationFS newAnnotation = indexedAnnos.get(index);
                        ((Annotation) newAnnotation).setEnd(aTokenStart + aToken.length());

                        index++;
                    }
                    else {
                        aAnnotations.put(layer, null);
                        index++;
                    }
                }
                lastIndex = index - 1;
            }
            // tokens annotated as B-X B-X, no I means it is end by itself
            for (int i = 1; i <= lastIndex; i++) {
                if (aBeginEndAnno.get(layer) != null && aBeginEndAnno.get(layer).get(i) != null
                        && aBeginEndAnno.get(layer).get(i).equals("B-")) {
                    aBeginEndAnno.get(layer).put(i, "E-");
                }
            }
        }
    }

    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /*
     * public static final String MULTIPLE_SPAN_ANNOTATIONS = "multipleSpans";
     *
     * @ConfigurationParameter(name = MULTIPLE_SPAN_ANNOTATIONS, mandatory = true, defaultValue =
     * {}) private List<String>multipleSpans;
     */

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);
        InputStream is = null;
        try {
            is = res.getInputStream();
            convertToCas(aJCas, is, encoding);
        }
        finally {
            closeQuietly(is);
        }

    }
}