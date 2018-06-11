package com.arunge.nlp.api;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

public class TokenForms {

    /**
     * 
     *<p>Functional interface use in extracting the final form of an {@link AnnotatedToken}.<p>
     *
     * @author Andrew Runge
     *
     */
    public interface TokenForm extends Function<AnnotatedToken, String>, Serializable {}
    
    /**
     * Returns the raw form of the token.
     * @return
     */
    public static TokenForm rawText() {
        return t -> t.text();
    }
    
    /**
     * Returns the lowercase'd form of the token.
     * @return
     */
    public static TokenForm lowercase() { 
        return t -> t.text().toLowerCase();
    }
    
    /**
     * Returns the form of the token as <seg_id>_<lowercase_token>, or simply the lowercase token if there is no segment ID.
     * @return
     */
    public static TokenForm lowercaseSegmented() { 
        return token -> {
            Optional<String> segmentAnn = token.getAnnotation(Annotator.SEGMENT);
            if(segmentAnn.isPresent()) {
                return segmentAnn.get() + "_" + token.text().toLowerCase();
            }
            return token.text().toLowerCase();
        };
    }
    
    /**
     * Returns the lowercase lemma of the token.
     * @return
     */
    public static TokenForm lemma() { 
        return token -> token.getAnnotation(Annotator.LEMMA).get().toLowerCase();
    }
    
    public static TokenForm splitLemma() { 
        return token -> {
            Optional<String> segmentAnn = token.getAnnotation(Annotator.SEGMENT);
            if(segmentAnn.isPresent()) { 
                return segmentAnn.get() + "_" + token.getAnnotation(Annotator.LEMMA).get().toLowerCase();
            }
            return token.text().toLowerCase();
        };
        
    }
    
    /**
     * Returns the lowercase stem of the token.
     * @return
     */
    public static TokenForm stem() { 
        return token -> token.getAnnotation(Annotator.STEM).get().toLowerCase();
    }
    
}
