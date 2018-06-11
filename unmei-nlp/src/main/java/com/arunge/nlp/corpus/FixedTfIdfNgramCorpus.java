package com.arunge.nlp.corpus;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureIndexer;
import com.arunge.nlp.corpus.transform.TFType;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;
import com.arunge.nlp.vocab.CountingNGramIndexer;

/**
 * 
 *<p>class_comment_here<p>
 *
 * @author Andrew Runge
 *
 */
@Deprecated
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
        CorpusDocument document = convert(doc, false);
        this.documents.add(document);
        if(document.getLabel() != null && !document.getLabel().isEmpty()) { 
            this.classLabels.add(document.getLabel());    
        }
        return documents.size() - 1;
    }
    
    /**
     * Converts a tokenized TextDocument into an {@link CorpusDocument} without adding it to the corpus.
     * 
     * @param doc
     * @param tokens
     * @return
     */
    public CorpusDocument convert(AnnotatedTextDocument doc) { 
        return convert(doc, true);
    }
    
    /**
     * Converts a tokenized TextDocument into an {@link CorpusDocument}. Optionally performs TF-IDF weighting
     * on the document to account for whether the document is being added to the corpus or not.
     * @param doc
     * @param tokens
     * @param applyWeights
     * @return
     */
    private CorpusDocument convert(AnnotatedTextDocument doc, boolean applyWeights) {
        CorpusDocument document = new CorpusDocument(doc.getDocId(), order);
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
                    document.addOrIncrementNgram(uniIndex, 1);
                    if(order >= 2 && i >= 1) {
                        int biIndex = indexer.getIndex(tokenFormExtractor.apply(sentence.get(i - 1)), 
                                tokStr);
                        if(biIndex == -1) {
                            continue;
                        }
                        document.addOrIncrementNgram(biIndex, 2);
                    }
                    if(order >= 3 && i >= 2) {
                        int triIndex = indexer.getOrAdd(tokenFormExtractor.apply(sentence.get(i - 2)), 
                                tokenFormExtractor.apply(sentence.get(i - 1)),
                                tokStr);
                        if(triIndex == -1) {
                            continue;
                        }
                        document.addOrIncrementNgram(triIndex, 3);
                    }
                }
            }
        }

        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getIndex(feat.getKey());
            if(featIndex != -1) {
                document.setFeature(featIndex, feat.getValue());
            }
        }
        if(applyWeights) { 
            if(type == TFType.LENGTH_NORM) {
                document = document.buildLengthNormCountDoc();
            } else if(type == TFType.LOG_LENGTH_NORM) {
                document = document.buildLogLengthNormCountDoc();
            }
            
            double[] idfCounts = indexer.computeIDFVector();
            for(int o = 1; o <= document.getOrder(); o++) {
                Map<Integer, Double> ngramCounts = document.getNgrams(o);
                for(int key : ngramCounts.keySet()) {
                    double tfidf = ngramCounts.get(key) * idfCounts[key];
                    document.setNgramValue(key, o, tfidf);
                }
            }
        }
        return document;
    }
    
}
