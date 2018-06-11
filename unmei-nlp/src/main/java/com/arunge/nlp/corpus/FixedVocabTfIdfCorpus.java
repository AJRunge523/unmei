package com.arunge.nlp.corpus;

import java.util.List;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.corpus.transform.TFType;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;
import com.arunge.nlp.vocab.CountingVocabulary;

/**
 * 
 *<p>Corpus with a pre-set Vocabulary. Documents added to this corpus that contain words
 *   not in the provided vocabulary will have those words filtered out.<p>
 *
 * @deprecated Use FixedTfIdfNgramCorpus with order 1 instead
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
        for(AnnotatedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                for(AnnotatedToken t : sentence) {
                    int index = vocab.getIndex(tokenFormExtractor.apply(t));
                    if(index == -1) {
                        continue;
                    }
                    document.addOrIncrementNgram(index, 1);
                }
            }
        }
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getOrAdd(feat.getKey());
            document.setFeature(featIndex, feat.getValue());
        }
        this.documents.add(document);
        this.classLabels.add(label);
        return documents.size() - 1;
    }

}
