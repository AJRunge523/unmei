package com.arunge.nlp.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.corpus.transform.TFType;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;
import com.arunge.nlp.vocab.CountingVocabulary;
import com.arunge.nlp.vocab.Vocabulary;

/**
 * 
 *<p>Extension of {@link Corpus} that uses a bag-of-words model with tf-idf counts for documents.
 *   This corpus uses corpus-level statistics, namely the Inverse Document Frequency terms, which
 *   can only be computed once the whole corpus has been observed. As such, it is not permitted
 *   to add documents to the corpus once the getDocuments() method has been invoked once.
 *
 *<p>
 *
 * @deprecated Use TfIdfNgramCorpus with order 1 instead.
 *
 * @author Andrew Runge
 *
 */
public class TfIdfCorpus extends Corpus {

    private static final long serialVersionUID = 8821097798507813708L;

    // Vocabulary should be serialized separately
    protected transient CountingVocabulary vocab;
    private TFType type;
    protected List<CorpusDocument> documents;
    
    public TfIdfCorpus() { 
        this(TFType.LENGTH_NORM);
    }
    
    public TfIdfCorpus(TFType type) { 
        super();
        this.vocab = new CountingVocabulary();
        this.type = type;
        this.documents = new ArrayList<>();
    }
    
    @Override
    public int addTokenizedDocument(AnnotatedTextDocument doc) {
        if(finalized) {
            throw new UnsupportedOperationException("Cannot add additional documents to the corpus after tf-idf counts have been computed.");
        }
        CorpusDocument document = new CorpusDocument(doc.getDocId());
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        for(AnnotatedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(AnnotatedToken t : sentence) {
                    int index = vocab.getOrAdd(t.text().toLowerCase());
                    boolean added = document.addOrIncrementNgram(index, 1);
                    if(added) { 
                        vocab.incrementDocFrequency(index);
                    }
                }
            }
        }
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getOrAdd(feat.getKey());
            document.setFeature(featIndex, feat.getValue());
        }
        vocab.incrementNumDocs();
        this.documents.add(document);
        this.classLabels.add(label);
        return documents.size() - 1;
    }
    
    @Override
    public Vocabulary getVocabulary() {
        return vocab;
    }
    
    @Override
    public List<CorpusDocument> getDocuments() {
        if(!finalized) {
            finalize();
        }
        return documents;
    }
    
    @Override
    public int size() {
        return documents.size();
    }
    
    @Override
    public void finalize() {
        super.finalize();
        computeTfIdfCounts();
    }
    
    private boolean computeTfIdfCounts() {
        double[] idfCounts = vocab.computeIDFVector();
        for(int i = 0; i < documents.size(); i++) {
            CorpusDocument d = documents.get(i);
            if(type == TFType.LENGTH_NORM) {
               d = d.buildLengthNormCountDoc();
            } else if(type == TFType.LOG_LENGTH_NORM) {
                d = d.buildLogLengthNormCountDoc();
            }
            Map<Integer, Double> vocab = d.getNgrams(1);
            for(int w : vocab.keySet()) {
                double tfidf = vocab.get(w) * idfCounts[w];
                d.setNgramValue(w, 1, tfidf);
            }
            documents.set(i, d);
        }
        return true;
    }

    @Override
    public void trimTail(int minCount, int minDocs) {
       CountingVocabulary newVocab = new CountingVocabulary(vocab);
       newVocab = newVocab.trimTail(minCount, minDocs);
        for(int i = 0; i < documents.size(); i++) {
            CorpusDocument d = documents.get(i);
            CorpusDocument newDoc = new CorpusDocument(d.getDocId());
            newDoc.setLabel(d.getLabel());
            newDoc.setFeatures(d.getFeatures());
            Map<Integer, Double> docVocab = d.getNgrams(1);
            for(Integer wordID : docVocab.keySet()) {
                int newWordID = newVocab.getIndex(vocab.getWord(wordID));
                if(newWordID != -1) {
                    newDoc.addOrIncrementNgram(newWordID, 1, d.getNgramValue(wordID, 1));
                }
            }
            documents.set(i, newDoc);
        }
        this.vocab = newVocab;
    }
    
}
