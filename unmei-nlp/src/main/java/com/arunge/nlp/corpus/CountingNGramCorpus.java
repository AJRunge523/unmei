package com.arunge.nlp.corpus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureIndexer;
import com.arunge.nlp.api.TokenForms;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.NGramIterator;
import com.arunge.nlp.vocab.CountingNGramIndexer;
import com.arunge.nlp.vocab.NGramIndexer;
import com.arunge.nlp.vocab.Vocabulary;

public class CountingNGramCorpus extends NGramCorpus {

    private static final long serialVersionUID = 8848756745985384534L;
    protected List<CorpusDocument> documents;
    protected transient CountingNGramIndexer indexer;
    protected int order;
    protected boolean indexOnly;
    protected boolean freezeVocab;
    protected boolean freezeFeatures;
    
    public CountingNGramCorpus(int ngramOrder) {
        this(ngramOrder, false);
    }
    
    public CountingNGramCorpus(int ngramOrder, boolean indexOnly) { 
        super();
        this.indexer = new CountingNGramIndexer(ngramOrder);
        this.order = ngramOrder;
        this.documents = new ArrayList<>();
        this.indexOnly = indexOnly;
        this.tokenFormExtractor = TokenForms.lowercaseSegmented();
    }
    
    public CountingNGramCorpus(CountingNGramIndexer indexer) {
        this.indexer = indexer;
        this.order = indexer.getOrder();
        this.documents = new ArrayList<>();
        this.indexOnly = false;
        this.tokenFormExtractor = TokenForms.lowercaseSegmented();
    }
    
    public CountingNGramCorpus(CountingNGramIndexer indexer, FeatureIndexer featureIndexer) { 
        this.indexer = indexer;
        this.order = indexer.getOrder();
        this.documents = new ArrayList<>();
        this.indexOnly = false;
        this.tokenFormExtractor = TokenForms.lowercaseSegmented();
        this.featureIndexer = featureIndexer;
        
    }
    
    /**
     * Set the corpus to only index the words in the documents without storing the documents or contents.
     * @param indexOnly
     * @return
     */
    public void indexOnly(boolean indexOnly) {
        this.indexOnly = indexOnly;
    }
    
    /**
     * If the vocab is frozen, new words and ngrams that have not been previously encountered
     * will be ignored instead of indexed and added to the corpus documents. Counts for vocabulary
     * will be frozen as well.
     * @param flag
     */
    public void freezeVocab(boolean flag) { 
        this.freezeVocab = flag;
        this.getNgramIndexer().freezeVocab();
    }
    
    /**
     * If the features are frozen, new features on the documents that have not been previously
     * encountered will be ignored instead of indexed and added to the corpus documents.
     * @param flag
     */
    public void freezeFeatures(boolean flag) { 
        this.freezeFeatures = flag;
    }
    
    @Override
    public int addTokenizedDocument(AnnotatedTextDocument doc) {
        if(finalized) {
            throw new UnsupportedOperationException("Cannot add additional documents to the corpus after corpus has been finalized.");
        }
        CorpusDocument document = new CorpusDocument(doc.getDocId(), order);
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        
        NGramIterator ngramIter = new NGramIterator(doc, this.tokenFormExtractor, this.order);
        while(ngramIter.hasNext()) { 
            String[] ngram = ngramIter.next();
            int index;
            if(freezeVocab) {
                index = indexer.getIndex(ngram);
                if(index == -1) {
                    continue;
                }
            } else {
                index = indexer.getOrAdd(ngram);
            }
            boolean added = document.addOrIncrementNgram(index, ngram.length);
            indexer.incrementNgramFrequency(index);
            if(added) {
                indexer.incrementDocFrequency(index);
            }
        }
        
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex;
            if(freezeFeatures) { 
                featIndex = featureIndexer.getIndex(feat.getKey());
                if(featIndex == -1) {
                    continue;
                }
            } else {
                featIndex = featureIndexer.getOrAdd(feat.getKey());
            }
            document.setFeature(featIndex, feat.getValue());
        }
        indexer.incrementNumDocs();
        if(!indexOnly) {
            this.documents.add(document);
        }
        this.classLabels.add(label);
        return documents.size() - 1;
    }

    @Override
    public Vocabulary getVocabulary() {
        return indexer.getVocabulary();
    }

    @Override
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
    
    public void finalize() {
        this.finalized = true;
    }
    
    @Override
    public int size() {
        return documents.size();
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
}
