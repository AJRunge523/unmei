package com.arunge.nlp.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureExtractor;
import com.arunge.nlp.api.FeatureWeightType;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.PreprocessedTextField;

public class TerminologyFeatureExtractor implements FeatureExtractor<PreprocessedTextDocument> {

    private List<String[]> terms;
    private List<String> featNames;
    private Map<String, List<TermEntry>> termLookup;
    
    public TerminologyFeatureExtractor() {
        this("src/main/resources/terminology.txt");
    }
    
    public TerminologyFeatureExtractor(String terminologyFile) {
        loadTerms(terminologyFile);
    }
    
    @Override
    public Map<FeatureDescriptor, Double> extractFeatures(PreprocessedTextDocument input) {
        long start = System.currentTimeMillis();
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        int featCount = 0;
        //Check if features should be split by text field
        int size = input.getLength();
        for(String fieldName : input.getTextFields().keySet()) {
            PreprocessedTextField field = input.getField(fieldName);
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(int i = 0; i < sentence.size(); i++) {
                    AnnotatedToken token = sentence.get(i);
                    String tokKey = token.getAnnotation(Annotator.LEMMA).isPresent() ? token.getAnnotation(Annotator.LEMMA).get() : token.text().toLowerCase();
                    if(termLookup.containsKey(tokKey)) {
                        List<TermEntry> termSets = termLookup.get(tokKey);
                        for(TermEntry termEntry : termSets) {
                            int j = i + 1;
                            while(j < sentence.size() && j - i < termEntry.words.length) {
                                AnnotatedToken nextTok = sentence.get(j);
                                String nextTokKey = nextTok.getAnnotation(Annotator.LEMMA).isPresent() ? nextTok.getAnnotation(Annotator.LEMMA).get() : nextTok.text().toLowerCase();
                                if(!nextTokKey.startsWith(termEntry.words[j - i])) {
                                    break;
                                }
                                j++;
                            }
                            //Indicates a matching term
                            if(j - i == termEntry.words.length) {
                                String f = featNames.get(termEntry.index);
                                Optional<String> segmentAnnotation = token.getAnnotation(Annotator.SEGMENT);
                                if(segmentAnnotation.isPresent()) {
                                    f = segmentAnnotation.get() + "_" + f;
                                }
                                FeatureDescriptor featDesc = new FeatureDescriptor(f, FeatureWeightType.TFIDF);
                                if(!features.containsKey(featDesc)) {
                                    features.put(featDesc, 0.0);
                                }
                                features.put(featDesc, features.get(featDesc) + (1.0 / size));
                                featCount += 1;
                            } 
                        }
                    }
                }
            }
        }
//        System.out.println("Extracted " + featCount + " features in " + (System.currentTimeMillis() - start) + " millis.");
        return features;
    }
    
    private void loadTerms(String filename) {
        terms = new ArrayList<>();
        featNames = new ArrayList<>();
        termLookup = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))) {
            
            String line = "";
            
            while((line = reader.readLine()) != null) {
                String[] termWords = line.split(" ");
                featNames.add("term_" + line.replaceAll(" ", "_"));
                terms.add(termWords);
                if(!termLookup.containsKey(termWords[0])) {
                    termLookup.put(termWords[0], new ArrayList<>());
                }
                termLookup.get(termWords[0]).add(new TermEntry(terms.size() - 1, termWords));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load terminology list.");
        }
        
    }
    
    private class TermEntry {
        
        int index;
        String[] words;
        
        public TermEntry(int index, String[] words) {
            this.words = words;
            this.index = index;
        }
        
    }
    
}
