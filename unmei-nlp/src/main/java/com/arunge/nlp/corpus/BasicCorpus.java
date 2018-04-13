package com.arunge.nlp.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;
import com.arunge.nlp.vocab.Vocabulary;

/**
 * 
 *<p>A basic implementation of a {@link Corpus} that represents documents via a bag of words model with raw counts.<p>
 *
 * @author Andrew Runge
 *
 */
public class BasicCorpus extends Corpus {

    private static final long serialVersionUID = 4977750817156321818L;
    private Vocabulary vocab;
    private List<CorpusDocument> documents;
    
    public BasicCorpus() { 
        super();
        this.vocab = new Vocabulary();
        this.documents = new ArrayList<>();
    }
    
    @Override
    public int addTokenizedDocument(AnnotatedTextDocument doc)  {
        CorpusDocument document = new CorpusDocument(doc.getDocId());
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        document.setLength(doc.getLength());
        for(AnnotatedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(AnnotatedToken token : sentence) {
                    int index = vocab.getOrAdd(tokenFormExtractor.apply(token));
                    document.addOrIncrementWord(index);
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
    public Vocabulary getVocabulary() { 
        return vocab;
    }

    @Override
    public void trimTail(int minInclusion) {
        throw new UnsupportedOperationException("Cannot perform count-based operations on a basic corpus");
    }

    @Override
    public List<CorpusDocument> getDocuments() {
        return documents;
    }

    @Override
    public int size() {
        return documents.size();
    }
    
}
