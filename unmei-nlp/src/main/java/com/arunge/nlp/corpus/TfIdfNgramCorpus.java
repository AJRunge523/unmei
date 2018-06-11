package com.arunge.nlp.corpus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.TokenForms;
import com.arunge.nlp.corpus.transform.TFType;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.NGramIterator;
import com.arunge.nlp.vocab.CountingNGramIndexer;
import com.arunge.nlp.vocab.NGramIndexer;
import com.arunge.nlp.vocab.Vocabulary;

/**
 * 
 *<p>Corpus with built-in support for computing tfidf weights over the terms
 *   in its documents.<p>
 *
 * @author Andrew Runge
 *
 * @deprecated Use {@link CountingNGramCorpus} with a {@link TfidfCorpusTransformer} instead.
 *
 */
@Deprecated
public class TfIdfNgramCorpus extends Corpus {

    private static Logger LOG = LoggerFactory.getLogger(TfIdfNgramCorpus.class);
    
    private static final long serialVersionUID = 8848756745985384534L;
    protected List<CorpusDocument> documents;
    protected transient CountingNGramIndexer indexer;
    protected TFType type;
    protected int order;
    
    public TfIdfNgramCorpus(int ngramOrder) {
        this(ngramOrder, TFType.LENGTH_NORM);
    }
    
    public TfIdfNgramCorpus(int ngramOrder, TFType type) { 
        super();
        this.indexer = new CountingNGramIndexer(ngramOrder);
        this.type = type;
        this.order = ngramOrder;
        this.documents = new ArrayList<>();
        this.tokenFormExtractor = TokenForms.lowercaseSegmented();
    }
    
    @Override
    public int addTokenizedDocument(AnnotatedTextDocument doc) {
        if(finalized) {
            throw new UnsupportedOperationException("Cannot add additional documents to the corpus after tf-idf counts have been computed.");
        }
        CorpusDocument document = new CorpusDocument(doc.getDocId(), order);
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        NGramIterator ngramIter = new NGramIterator(doc, this.tokenFormExtractor, this.order);
        while(ngramIter.hasNext()) { 
            String[] ngram = ngramIter.next();
            int index = indexer.getOrAdd(ngram);
            boolean added = document.addOrIncrementNgram(index, ngram.length);
            indexer.incrementNgramFrequency(index);
            if(added) {
                indexer.incrementDocFrequency(index);
            }
        }
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getOrAdd(feat.getKey());
            document.setFeature(featIndex, feat.getValue());
        }
        indexer.incrementNumDocs();
        this.documents.add(document);
        this.classLabels.add(label);
        return documents.size() - 1;
    }

    @Override
    public Vocabulary getVocabulary() {
        return indexer.getVocabulary();
    }

    public NGramIndexer getNgramIndexer() {
        return indexer;
    }
    
    @Override
    public List<CorpusDocument> getDocuments() {
        if(!finalized) {
            finalize();
        }
        List<CorpusDocument> docs = new ArrayList<>();
        docs.addAll(documents);
        return docs;
    }
    
    @Override
    public int size() {
        return documents.size();
    }
    
    @Override
    public void trimTail(int minCount, int minDocs) {
        CountingNGramIndexer newIndexer = indexer.trimTail(minCount, minDocs);
        for(int i = 0; i < documents.size(); i++) {
            CorpusDocument d = documents.get(i);
            CorpusDocument newDoc = new CorpusDocument(d.getDocId(), d.getOrder());
            newDoc.setLabel(d.getLabel());
            newDoc.setFeatures(d.getFeatures());
            for(int o = 1; o <= d.getOrder(); o++) {
                Map<Integer, Double> ngramCounts = d.getNgrams(o);
                for(int key : ngramCounts.keySet()) {
                    String[] ngram = indexer.getNgram(key);
                    int newNgramIndex = newIndexer.getIndex(ngram);
                    if(newNgramIndex != -1 ) {
                        newDoc.addOrIncrementNgram(newNgramIndex, o, ngramCounts.get(key));
                    }
                }
            }
            documents.set(i, newDoc);
        }
        this.indexer = newIndexer;
    }
    
    @Override
    public void finalize() {
        super.finalize();
        computeTfIdfCounts();
    }
    
    private boolean computeTfIdfCounts() {
        LOG.info("COMPUTING TFIDF COUNTS");
        double[] idfCounts = indexer.computeIDFVector();
        for(int i = 0; i < documents.size(); i++) {
            CorpusDocument d = documents.get(i);
            if(type == TFType.LENGTH_NORM) {
                d = d.buildLengthNormCountDoc();
            } else if(type == TFType.LOG_LENGTH_NORM) {
                d = d.buildLogLengthNormCountDoc();
            }
            for(int o = 1; o <= d.getOrder(); o++) {
                Map<Integer, Double> ngramCounts = d.getNgrams(o);
                for(int key : ngramCounts.keySet()) {
                    double tfidf = ngramCounts.get(key) * idfCounts[key];
                    d.setNgramValue(key, o, tfidf);
                }
            }
            documents.set(i, d);
        }
        return true;
    }
    
    @Override
    public void export(String outputPath, String fileName) throws IOException {
        if(!finalized) {
            finalize();
        }
        indexer.write(new File(outputPath + "\\" + fileName + ".vocab"));
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(outputPath + "\\" + fileName + ".corpus")))) {
            out.writeObject(this);
        }
        
    }
    
}
