package com.arunge.nlp.vocab;

import java.util.List;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;

/**
 * 
 *<p>Corpus with a pre-set Vocabulary. Documents added to this corpus that contain words
 *   not in the provided vocabulary will have those words filtered out.<p>
 *
 * @author Andrew Runge
 *
 */
public class FixedVocabTfIdfCorpus extends TfIdfCorpus{

    private static final long serialVersionUID = -6407355498606370827L;

    public FixedVocabTfIdfCorpus(CountingVocabulary vocab) {
        super();
        this.vocab = vocab;
    }

    public FixedVocabTfIdfCorpus(CountingVocabulary vocab, TFType type) {
        super(type);
        this.vocab = vocab;
    }
    
    @Override
    public int addTokenizedDocument(AnnotatedTextDocument doc) {
        if(finalized) {
            throw new UnsupportedOperationException("Cannot add additional documents to the corpus after tf-idf counts have been computed.");
        }
        CorpusDocument document = new CorpusDocument(doc.getDocId());
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        int length = 0;
        for(AnnotatedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(AnnotatedToken t : sentence) {
                    int index = vocab.getIndex(tokenFormExtractor.apply(t));
                    if(index == -1) {
                        continue;
                    }
                    document.addOrIncrementWord(index);
                    length += 1;
                }
            }
        }
        document.setLength(length);
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getOrAdd(feat.getKey());
            document.addFeature(featIndex, feat.getValue());
        }
        this.documents.add(document);
        this.classLabels.add(label);
        return documents.size() - 1;
    }

}
