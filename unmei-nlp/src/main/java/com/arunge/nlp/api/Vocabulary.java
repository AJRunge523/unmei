package com.arunge.nlp.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * 
 *<p>Store vocabulary from a corpus of documents.<p>
 *
 * @author Andrew Runge
 *
 */
public class Vocabulary extends StringIndexer {

    private static final long serialVersionUID = -9043467805009023608L;
    protected static String SEP = String.valueOf((char) 31);
    protected boolean frozen;
    
    public Vocabulary() { 
        super();
        this.frozen = false;
    }
    
    public Vocabulary(int initSize) {
        super(initSize);
        this.frozen = false;
    }
    
    public Vocabulary(Vocabulary other) {
        super(other);
        this.frozen = other.frozen;
    }
    
    @Override
    public int getOrAdd(String word) {
        if(!vocab.containsKey(word)) {
            if(frozen) {
                return -1;
            } else {
                vocab.put(word, index2Word.size());
                index2Word.add(word);
            }
        }
        return vocab.get(word);
    }
    
    public List<String> getVocabWords() {
        return Collections.unmodifiableList(index2Word);
    }
    
    public boolean isFrozen() {
        return frozen;
    }
    
    /**
     * Freezes the contents of the vocabulary, allowing no other items to be added to it.
     */
    public void freezeVocab() {
        this.frozen = true;
    }
    
    public void write(OutputStream out) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(out);
        stream.writeObject(this);
    }
    
    public void write(File f) throws IOException {
        write(new FileOutputStream(f));
    }
    
    public static Vocabulary read(File w) throws IOException {
        return read(new FileInputStream(w));
    }
    
    public static Vocabulary read(InputStream in) throws IOException {
        ObjectInputStream stream = new ObjectInputStream(in);
        try {
            return (Vocabulary) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to deserialize vocabulary", e);
        }
    }
    
}
