package com.arunge.nlp.vocab;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureIndexer;
import com.arunge.nlp.api.NGramCorpusDocument;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;

public class FixedTfIdfNgramCorpus extends TfIdfNgramCorpus {

    private static final long serialVersionUID = -4551507279092630221L;

    public FixedTfIdfNgramCorpus(CountingNGramIndexer ngramIndexer, FeatureIndexer featureIndexer) { 
        super(ngramIndexer.getOrder());
        this.indexer = ngramIndexer;
        this.featureIndexer = featureIndexer;
    }
    
    @Override
    public int addTokenizedDocument(AnnotatedTextDocument doc) {
        if(finalized) {
            throw new UnsupportedOperationException("Cannot add additional documents to the corpus after tf-idf counts have been computed.");
        }
        NGramCorpusDocument document = convert(doc, false);
        this.documents.add(document);
        if(document.getLabel() != null && !document.getLabel().isEmpty()) { 
            this.classLabels.add(document.getLabel());    
        }
        return documents.size() - 1;
    }
    
    /**
     * Converts a tokenized TextDocument into an {@link NGramCorpusDocument} without adding it to the corpus.
     * 
     * @param doc
     * @param tokens
     * @return
     */
    public NGramCorpusDocument convert(AnnotatedTextDocument doc) { 
        return convert(doc, true);
    }
    
    /**
     * Converts a tokenized TextDocument into an {@link NGramCorpusDocument}. Optionally performs TF-IDF weighting
     * on the document to account for whether the document is being added to the corpus or not.
     * @param doc
     * @param tokens
     * @param applyWeights
     * @return
     */
    private NGramCorpusDocument convert(AnnotatedTextDocument doc, boolean applyWeights) {
        NGramCorpusDocument document = new NGramCorpusDocument(doc.getDocId(), order);
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        for(AnnotatedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(int i = 0; i < sentence.size(); i++) {
                    AnnotatedToken tok = sentence.get(i);
                    String tokStr = tokenFormExtractor.apply(tok);
                    int uniIndex = indexer.getIndex(tokStr);
                    if(uniIndex == -1) {
                        continue;
                    }
                    document.addOrIncrementWord(uniIndex);
                    document.setLength(document.getLength() + 1);
                    if(order >= 2 && i >= 1) {
                        int biIndex = indexer.getIndex(tokenFormExtractor.apply(sentence.get(i - 1)), 
                                tokStr);
                        if(biIndex == -1) {
                            continue;
                        }
                        document.addOrIncrementNgram(biIndex, 2);
                        document.setNgramLength(document.getNgramLength(2) + 1, 2);
                    }
                    if(order >= 3 && i >= 2) {
                        int triIndex = indexer.getOrAdd(tokenFormExtractor.apply(sentence.get(i - 2)), 
                                tokenFormExtractor.apply(sentence.get(i - 1)),
                                tokStr);
                        if(triIndex == -1) {
                            continue;
                        }
                        document.addOrIncrementNgram(triIndex, 3);
                        document.setNgramLength(document.getNgramLength(3) + 1, 3);
                    }
                }
            }
        }

        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getIndex(feat.getKey());
            if(featIndex != -1) {
                document.addFeature(featIndex, feat.getValue());
            }
        }
        if(applyWeights) { 
            if(type == TFType.LENGTH_NORM) {
                document.buildLengthNormCountDoc();
            } else if(type == TFType.LOG_LENGTH_NORM) {
                document.buildLogLengthNormCountDoc();
            }
            double[][] idfCounts = indexer.computeIDFVector();
            for(int o = 1; o <= document.getOrder(); o++) {
                Map<Integer, Double> ngramCounts = document.getNgrams(o);
                for(int key : ngramCounts.keySet()) {
                    double tfidf = ngramCounts.get(key) * idfCounts[o - 1][key];
                    document.setNgramCount(key, o, tfidf);
                }
            }
        }
        return document;
    }
    
}
