package com.arunge.nlp.stanford;

import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordTest {

    public static void main(String[] args) throws Exception {
        Annotation document = new Annotation("Computer-implemented data points are the future. Neural networks have frequenly been used in maximum entropy models.");
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        pipeline.annotate(document);
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) + ", " + sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
            for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.println(label.word() + ", " + label.get(CoreAnnotations.PartOfSpeechAnnotation.class));
//                System.out.println(label.get(CoreAnnotations.PartOfSpeechAnnotation.class));
            }
        }
      }
    
}
