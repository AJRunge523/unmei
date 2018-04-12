package com.arunge.nlp.stanford;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.Token;
import com.arunge.nlp.processors.BasicNLPPreprocessingPipeline;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;
import com.arunge.nlp.text.TextDocument;
import com.arunge.nlp.tokenization.TokenSplitter;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordNLPPreprocessingPipeline extends BasicNLPPreprocessingPipeline {

    private StanfordCoreNLP pipeline;
    
    public StanfordNLPPreprocessingPipeline(Annotator...annotators) {
        
        String annotatorList = "tokenize,ssplit";
        Arrays.sort(annotators);
        for(Annotator annotator : annotators) {
            switch(annotator) {
            case POS:
                tagger = new StanfordPOSTagger();
                break;
            case LEMMA:
                lemma = new StanfordLemmatizer();
                break;
//            case NER:
//                annotatorList += ",ner";
//                break;
//            case DEPPARSE:
//                annotatorList += ",depparse";
//                break;
            default:
                break;
            }
        }
//        annotatorList += ",parse,mention,coref";
//        System.out.println(annotatorList);
        Properties props = new Properties();
        props.setProperty("annotators",  annotatorList);
//        props.setProperty("coref.algorithm", "neural");

        pipeline = new StanfordCoreNLP(props);
    }

    
    
    @Override
    public AnnotatedTextDocument apply(TextDocument doc) {
        AnnotatedTextDocument processed = new AnnotatedTextDocument(doc);
        for(String fieldName : doc.getFieldNames()) {
            AnnotatedTextField field = new AnnotatedTextField();
            String fieldText = doc.getTextField(fieldName);
            Annotation document = pipeline.process(fieldText);
            for(CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<AnnotatedToken> sentenceToks = new ArrayList<>();
                for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class)) { 
                    AnnotatedToken token = new AnnotatedToken(label.word(), label.beginPosition(), label.endPosition());
                    token.addAnnotation(Annotator.SEGMENT, fieldName);
                    sentenceToks.add(token);
                }
                sentenceToks = processTokens(sentenceToks);
                if(tagger != null) { 
                    tagger.tag(sentenceToks);
                }
                if(lemma != null) {
                    lemma.tag(sentenceToks);
                }
                if(stemmer != null) { 
                    stemmer.tag(sentenceToks);
                }
                
                field.addSentence(sentenceToks);
            }
            processed.addTextField(fieldName, field);
        }
        return processed;
    }
    
    private List<AnnotatedToken> processTokens(List<AnnotatedToken> tokens) {
        Stream<AnnotatedToken> tokStream = tokens.stream();
        for(TokenSplitter splitter : tokenSplitters) {
            tokStream = tokStream.flatMap(t -> { 
                Stream<Token> splitToks = splitter.apply(t);
                return splitToks.map(st -> new AnnotatedToken(st.text(), st.start(), st.end(), t.getAnnotations()));
            });
        }
        for(TokenFilter filter : tokenFilters) {
            tokStream = tokStream.filter(filter);
        }
        return tokStream.collect(Collectors.toList());
    }
    
//    @Override
//    public PreprocessedTextDocument apply(TextDocument doc) {
//        PreprocessedTextDocument processed = new PreprocessedTextDocument(doc);
//        for(String fieldName : doc.getFieldNames()) {
//            PreprocessedTextField field = new PreprocessedTextField();
//            String fieldText = doc.getTextField(fieldName);
//            Annotation document = pipeline.process(fieldText);
//            for(CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
//                List<AnnotatedToken> sentenceToks = new ArrayList<>();
//                for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                    AnnotatedToken token = new AnnotatedToken(label.word(), label.beginPosition(), label.endPosition());
//                    String pos = label.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                    if(pos != null) {
//                        token.addAnnotation(Annotator.POS, pos);
//                    }
//                    String lemma = label.lemma();
//                    if(lemma != null) {
//                        token.addAnnotation(Annotator.LEMMA, lemma);
//                    }
//                    String ner = label.ner();
//                    if(ner != null) {
//                        token.addAnnotation(Annotator.NER, ner);
//                    }
//                    token.addAnnotation(Annotator.SEGMENT, fieldName);
//                    sentenceToks.add(token);
//                }
//                field.addSentence(sentenceToks);
//            }
//            processed.addTextField(fieldName, field);
//        }
//        return processed;
//    }

    
}
