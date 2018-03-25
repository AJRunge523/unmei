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

public class CountingCorpus extends Corpus{

    private static final long serialVersionUID = -2881947250402363606L;
    private CountingVocabulary vocabulary;
    private List<CorpusDocument> documents;
    
    public CountingCorpus() { 
        super();
        this.vocabulary = new CountingVocabulary();
        this.documents = new ArrayList<>();
    }
    
    @Override
    public int addTokenizedDocument(PreprocessedTextDocument doc) {
        CorpusDocument document = new CorpusDocument(doc.getDocId());
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        document.setLength(doc.getLength());
        for(PreprocessedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(AnnotatedToken token : sentence) {
                    int index = vocabulary.getOrAdd(token.text().toLowerCase());
                    vocabulary.incrementWordFrequency(index);
                    boolean added = document.addOrIncrementWord(index);
                    if(added) {
                        vocabulary.incrementDocFrequency(index);
                    }
                }
            }
        }
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getOrAdd(feat.getKey());
            document.addFeature(featIndex, feat.getValue());
        }
        this.documents.add(document);
        this.classLabels.add(label);
        return documents.size() - 1;
    }

    @Override
    public List<CorpusDocument> getDocuments() {
        return documents;
    }

    @Override
    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    @Override
    public int size() {
        return documents.size();
    }

    @Override
    public void trimTail(int minInclusion) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Currently not implemented");
    }

}
