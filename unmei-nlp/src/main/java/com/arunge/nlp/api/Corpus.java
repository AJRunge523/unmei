package com.arunge.nlp.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.TextDocument;

/**
 * 
 *<p>Abstract representation of a corpus of documents. A corpus is defined by a set of documents and a vocabulary. Documents
 *   are represented by indices of words in the vocabulary with some measure for counting how many times each word appears
 *   in the document. 
 *   
 *   A <code>Corpus</code> is able to construct document counts in whatever manner is most appropriate for its purpose.
 *   <p>
 *
 * @author Andrew Runge
 *
 */
public abstract class Corpus implements Iterable<CorpusDocument>, Serializable {

    private static final long serialVersionUID = 3725392330208037564L;
    protected Set<String> classLabels;
    protected boolean finalized;
    protected FeatureIndexer featureIndexer;
    
    
    public Corpus() {
        this.classLabels = new HashSet<>();
        this.featureIndexer = new FeatureIndexer();
    }
    
//    /**
//     * Add a tokenized document with the specified ID to the corpus. Returns the numerical index of the document in the corpus.
//     * @param docId
//     * @param tokens
//     * @return
//     */
//    public int addTokenizedDocument(String docId, List<Token> tokens) {
//        PreprocessedTextDocument d = new PreprocessedTextDocument(docId);
//        return addTokenizedDocument(new PreprocessedTextDocument(docId, ""), tokens);
//    }
    
    /**
     * Add a tokenized document described by the provided {@link TextDocument} to the corpus and returns the numerical index of the document in the corpus.
     * @param docId
     * @param tokens
     */
    public abstract int addTokenizedDocument(PreprocessedTextDocument doc);
    
    /**
     * Adds a feature to this corpus (if not already present) and returns an index for use in storing values of the feature.
     * @param featureName
     * @return
     */
    public int addFeature(String featureName) { 
        return this.featureIndexer.getOrAdd(featureName);
    }
    
    public abstract List<CorpusDocument> getDocuments();

    public abstract Vocabulary getVocabulary();

    public Set<String> getClassLabels() {
        return classLabels;
    }
    
    public abstract int size();
    
    /**
     * Returns the indexer for structured features in the corpus.
     * @return
     */
    public FeatureIndexer getFeatures() {
        return featureIndexer;
    }
    
    public void export(String outputPath, String fileName) throws IOException {
        Vocabulary vocab = getVocabulary();
        vocab.write(new File(outputPath + "\\" + fileName + ".vocab"));
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(outputPath + "\\" + fileName + ".corpus")))) {
            out.writeObject(this);
        }
        
    }
    
    public static Corpus loadCorpus(File file) throws IOException {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            try {
                return (Corpus) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to deserialize corpus");
            }
        }
    }
    
    public abstract void trimTail(int minInclusion);
    
    /**
     * Certain types of corpora can execute corpus-wide operations that permanently modify their internal representations.
     * This method proides a way to indicate that the corpus is completed and that the user will no longer manually
     * modify its contents.
     */
    public void finalize() {
        this.finalized = true;
        
        //Perform tf-idf weight computation on corpus
        List<Integer> tfIdfFeatures = new ArrayList<>();
        for(Map.Entry<FeatureDescriptor, Integer> desc : featureIndexer) {
            if(desc.getKey().getWeightType().equals(FeatureWeightType.TFIDF)) {
                tfIdfFeatures.add(desc.getValue());
            }
        }
        double[] docFreqs = new double[tfIdfFeatures.size()];
        for(CorpusDocument d : getDocuments()) {
            for(int i = 0; i < tfIdfFeatures.size(); i++) {
                Optional<Double> featVal = d.getFeature(tfIdfFeatures.get(i));
                if(featVal.isPresent() && featVal.get() > 0 ) {
                    docFreqs[i] += 1;
                }
            }
        }
        int corpusSize = size();
        for(int i = 0; i < docFreqs.length; i++) {
            if(docFreqs[i] > 0) { 
                docFreqs[i] = Math.log(corpusSize / docFreqs[i]);
            }
        }
        for(CorpusDocument d : getDocuments()) {
            for(int i = 0; i < docFreqs.length; i++) {
                Optional<Double> val = d.getFeature(tfIdfFeatures.get(i));
                if(val.isPresent()) {
                    d.setFeature(tfIdfFeatures.get(i), val.get() * docFreqs[i]);
                }
            }
        }
    }
    
    @Override
    public Iterator<CorpusDocument> iterator() {
        return getDocuments().iterator();
    }
    
    
}
