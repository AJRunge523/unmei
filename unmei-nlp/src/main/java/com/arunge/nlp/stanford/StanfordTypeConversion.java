package com.arunge.nlp.stanford;

import java.util.List;
import java.util.stream.Collectors;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.Token;
import com.google.common.collect.Lists;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.ArrayCoreMap;

public class StanfordTypeConversion {

    public static <T extends Token> CoreLabel toCoreLabel(T token) { 
        CoreLabel label = new CoreLabel();
        label.setWord(token.text());
        label.setBeginPosition(token.start());
        label.setEndPosition(token.end());
        if(token instanceof AnnotatedToken) {
            AnnotatedToken annTok = (AnnotatedToken) token;
            if(annTok.hasAnnotation(Annotator.POS)) {
                label.setTag(annTok.getAnnotation(Annotator.POS).get());
            }
            if(annTok.hasAnnotation(Annotator.LEMMA)) {
                label.setLemma(annTok.getAnnotation(Annotator.LEMMA).get());
            }
            if(annTok.hasAnnotation(Annotator.NER)) {
                label.setNER(annTok.getAnnotation(Annotator.NER).get());
            }
        }
        return label;
    }
    
    public static Annotation toAnnotation(List<AnnotatedToken> sentence) { 
        ArrayCoreMap cm = new ArrayCoreMap();
        List<CoreLabel> coreLabels = sentence.stream().map(StanfordTypeConversion::toCoreLabel).collect(Collectors.toList());
        cm.set(CoreAnnotations.TokensAnnotation.class, coreLabels);
        return new Annotation(Lists.newArrayList(cm));
    }
    
    public static List<CoreLabel> toCoreLabels(List<? extends Token> sentence){ 
        List<CoreLabel> coreLabels = sentence.stream().map(StanfordTypeConversion::toCoreLabel).collect(Collectors.toList());
        return coreLabels;
    }
    
}
