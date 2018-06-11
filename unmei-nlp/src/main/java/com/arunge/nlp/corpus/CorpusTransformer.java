package com.arunge.nlp.corpus;

/**
 * 
 *<p>General interface for something that alters the contents of a corpus though some process. 
 *   The provided corpus should be modified in place. Implementing classes may place further restrictions
 *   on the type of corpus or document that they may operate on.<p>
 *
 * @author Andrew Runge
 *
 */
public interface CorpusTransformer {

    void transform(Corpus corpus);
    
    void transform(CorpusDocument doc);
    
}
