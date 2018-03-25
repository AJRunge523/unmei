package com.arunge.nlp.vocab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Corpus;
import com.arunge.nlp.api.CorpusDocument;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.Vocabulary;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.PreprocessedTextField;

/**
 * 
 *<p>Extension of {@link Corpus} that uses a bag-of-words model with tf-idf counts for documents.
 *   This corpus uses corpus-level statistics, namely the Inverse Document Frequency terms, which
 *   can only be computed once the whole corpus has been observed. As such, it is not permitted
 *   to add documents to the corpus once the getDocuments() method has been invoked once.
 *
 *<p>
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
    public int addTokenizedDocument(PreprocessedTextDocument doc) {
        if(finalized) {
            throw new UnsupportedOperationException("Cannot add additional documents to the corpus after tf-idf counts have been computed.");
        }
        CorpusDocument document = new CorpusDocument(doc.getDocId());
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        document.setLength(doc.getLength());
        for(PreprocessedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(AnnotatedToken t : sentence) {
                    int index = vocab.getOrAdd(t.text().toLowerCase());
                    boolean added = document.addOrIncrementWord(index);
                    if(added) { 
                        vocab.incrementDocFrequency(index);
                    }
                }
            }
        }
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getOrAdd(feat.getKey());
            document.addFeature(featIndex, feat.getValue());
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
            for(int w : d.getVocab().keySet()) {
                double tfidf = d.getVocab().get(w) * idfCounts[w];
                d.setWordCount(w, tfidf);
            }
            documents.set(i, d);
        }
        return true;
    }

    @Override
    public void trimTail(int minInclusion) {
       CountingVocabulary newVocab = new CountingVocabulary(vocab);
       newVocab = newVocab.trimTail(minInclusion);
        for(int i = 0; i < documents.size(); i++) {
            CorpusDocument d = documents.get(i);
            CorpusDocument newDoc = new CorpusDocument(d.getDocId());
            newDoc.setLabel(d.getLabel());
            newDoc.setFeatures(d.getFeatures());
            int newLength = 0;
            for(Integer wordID : d.getVocab().keySet()) {
                int newWordID = newVocab.getIndex(vocab.getWord(wordID));
                if(newWordID != -1) {
                    newDoc.addOrIncrementWord(newWordID, d.getWord(wordID));
                    newLength += d.getWord(wordID);
                }
            }
            newDoc.setLength(newLength);
            documents.set(i, newDoc);
        }
        this.vocab = newVocab;
    }
    
}
