package com.arunge.nlp.vocab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.Corpus;
import com.arunge.nlp.api.CorpusDocument;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.NGramCorpusDocument;
import com.arunge.nlp.api.NGramIndexer;
import com.arunge.nlp.api.Vocabulary;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.PreprocessedTextField;

public class CountingNGramCorpus extends Corpus {

    private static final long serialVersionUID = 8848756745985384534L;
    protected List<NGramCorpusDocument> documents;
    protected transient CountingNGramIndexer indexer;
    protected int order;
    private boolean indexOnly;
    
    public CountingNGramCorpus(int ngramOrder) {
        this(ngramOrder, false);
    }
    
    public CountingNGramCorpus(int ngramOrder, boolean indexOnly) { 
        super();
        this.indexer = new CountingNGramIndexer(ngramOrder);
        this.order = ngramOrder;
        this.documents = new ArrayList<>();
        this.indexOnly = indexOnly;
    }
    
    public CountingNGramCorpus(CountingNGramIndexer indexer) {
        this.indexer = indexer;
        this.order = indexer.getOrder();
        this.documents = new ArrayList<>();
        this.indexOnly = false;
    }
    
    /**
     * Set the corpus to only index the words in the documents without storing the documents or contents.
     * @param indexOnly
     * @return
     */
    public void indexOnly(boolean indexOnly) {
        this.indexOnly = indexOnly;
    }
    
    @Override
    public int addTokenizedDocument(PreprocessedTextDocument doc) {
        if(finalized) {
            throw new UnsupportedOperationException("Cannot add additional documents to the corpus after tf-idf counts have been computed.");
        }
        NGramCorpusDocument document = new NGramCorpusDocument(doc.getDocId(), order);
        String label = doc.getLabel().orElse("");
        document.setLabel(label);
        document.setLength(doc.getLength());
        for(PreprocessedTextField field : doc.getTextFields().values()) {
            for(List<AnnotatedToken> tokens : field.getSentences()) {
                for(int i = 0; i < tokens.size(); i++) {
                    AnnotatedToken tok = tokens.get(i);
                    String tokStr = getTokenString(tok);
                    int uniIndex = indexer.getOrAdd(tokStr);
                    indexer.incrementNgramFrequency(uniIndex, 1);
                    boolean added = document.addOrIncrementWord(uniIndex, 1);
                    if(added) {
                        indexer.incrementDocFrequency(uniIndex, 1);
                    }
                    if(order >= 2 && i >= 1) {
                        int biIndex = indexer.getOrAdd(getTokenString(tokens.get(i - 1)), 
                                tokStr);
                        added = document.addOrIncrementNgram(biIndex, 2);
                        indexer.incrementNgramFrequency(biIndex, 2);
                        if(added) {
                            indexer.incrementDocFrequency(biIndex, 2);
                        }
                    }
                    if(order >= 3 && i >= 2) {
                        int triIndex = indexer.getOrAdd(getTokenString(tokens.get(i - 2)), 
                                getTokenString(tokens.get(i - 1)),
                                tokStr);
                        indexer.incrementNgramFrequency(triIndex, 3);
                        added = document.addOrIncrementNgram(triIndex, 3);
                        if(added) {
                            indexer.incrementDocFrequency(triIndex, 3);
                        }
                    }
                }
            }
        }
        for(Entry<FeatureDescriptor, Double> feat : doc.getFeatures().entrySet()) {
            int featIndex = featureIndexer.getOrAdd(feat.getKey());
            document.addFeature(featIndex, feat.getValue());
        }
        indexer.incrementNumDocs();
        if(!indexOnly) {
            this.documents.add(document);
        }
        this.classLabels.add(label);
        return documents.size() - 1;
    }

    protected String getTokenString(AnnotatedToken token) {
        Optional<String> segmentAnn = token.getAnnotation(Annotator.SEGMENT);
        if(segmentAnn.isPresent()) {
            return segmentAnn.get() + "_" + token.text().toLowerCase();
        }
        return token.text().toLowerCase();
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
    public void trimTail(int minInclusion) {
        CountingNGramIndexer newIndexer = indexer.trimTail(minInclusion);
        for(int i = 0; i < documents.size(); i++) {
            NGramCorpusDocument d = documents.get(i);
            NGramCorpusDocument newDoc = new NGramCorpusDocument(d.getDocId(), d.getOrder());
            newDoc.setLabel(d.getLabel());
            newDoc.setFeatures(d.getFeatures());
            int newLength = 0;
            for(Integer wordID : d.getVocab().keySet()) {
                
                int newWordID = newIndexer.getIndex(indexer.getNgram(wordID, 1));
                if(newWordID != -1) {
                    newDoc.addOrIncrementWord(newWordID, d.getWord(wordID));
                    newLength += d.getWord(wordID);
                }
            }
            
            newDoc.setLength(newLength);
            for(int o = 2; o <= d.getOrder(); o++) {
                Map<Integer, Double> ngramCounts = d.getNgrams(o);
                int ngramLength = 0;
                for(int key : ngramCounts.keySet()) {
                    String[] ngram = indexer.getNgram(key, o);
                    int newNgramIndex = newIndexer.getIndex(ngram);
                    if(newNgramIndex != -1 ) {
                        newDoc.addOrIncrementNgram(newNgramIndex, o, ngramCounts.get(key));
                        ngramLength += ngramCounts.get(key);
                    }
                }
                newDoc.setNgramLength(ngramLength, o);
            }
            documents.set(i, newDoc);
        }
        this.indexer = newIndexer;
    }
}