package com.arunge.nlp.api;

import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.TextDocument;

/**
 * 
 *<p>Describes the core interface for a preprocessing pipeline for text documents composed of multiple sections.<p>
 *
 * @author Andrew Runge
 *
 */
public interface NLPPreprocessingPipeline {

    AnnotatedTextDocument apply(TextDocument doc);
 
}
