package com.arunge.nlp.text;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.arunge.nlp.api.TokenForms;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;

@DisplayName("An NGramIterator")
public class NGramIteratorTest {

    static AnnotatedTextDocument doc;
    
    @BeforeAll
    static void setup() { 
        TextDocument text = new TextDocument("test", "I met you before the fall of Rome. And I begged you to let me take you home.");
        StanfordNLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
        doc = pipeline.apply(text);
    }
    
    @Test
    @DisplayName("supports unigrams")
    void testUnigrams() {
        NGramIterator iter = new NGramIterator(doc, TokenForms.lowercase(), 1);
        String[] words = new String[] {"i", "met", "you", "before", "the", "fall", "of", "rome", ".", "and", "i", "begged", "you", 
                "to", "let", "me", "take", "you", "home", "."};
        
        int numNgrams = 0;
        while(iter.hasNext()) {
            String[] next = iter.next();
            String ngram = next.length > 1 ? next[0] : Arrays.stream(next).reduce((a, b) -> a + " " + b).get(); 
            assertThat(ngram, equalTo(words[numNgrams]));
            numNgrams += 1;
        }
        assertThat(numNgrams, equalTo(words.length));
    }
    
    @Test
    @DisplayName("supports bigrams")
    void testBigrams() { 
        NGramIterator iter = new NGramIterator(doc, TokenForms.lowercase(), 2);
        String[] words = new String[] {"i", "met", "i met", "you", "met you", "before", "you before", "the", "before the", "fall",
                "the fall", "of", "fall of", "rome", "of rome", ".", "rome .", "and", "i", "and i", "begged", "i begged", 
                "you", "begged you", "to", "you to", "let", "to let", "me", "let me", "take", "me take", "you", "take you", "home",
                "you home", ".", "home ." };
        int numNgrams = 0;
        while(iter.hasNext()) {
            String[] next = iter.next();
            String ngram = next.length == 1 ? next[0] : Arrays.stream(next).reduce((a, b) -> a + " " + b).get(); 
            assertThat(ngram, equalTo(words[numNgrams]));
            numNgrams += 1;
        }
        assertThat(numNgrams, equalTo(words.length));
    }
    
    @Test
    @DisplayName("supports bigrams crossing sentences")
    void testBigramsCrossSentence() { 
        NGramIterator iter = new NGramIterator(doc, TokenForms.lowercase(), 2, true);
        String[] words = new String[] {"i", "met", "i met", "you", "met you", "before", "you before", "the", "before the", "fall",
                "the fall", "of", "fall of", "rome", "of rome", ".", "rome .", "and", ". and", "i", "and i", "begged", "i begged", 
                "you", "begged you", "to", "you to", "let", "to let", "me", "let me", "take", "me take", "you", "take you", "home",
                "you home", ".", "home ." };
        int numNgrams = 0;
        while(iter.hasNext()) {
            String[] next = iter.next();
            String ngram = next.length == 1 ? next[0] : Arrays.stream(next).reduce((a, b) -> a + " " + b).get(); 
            assertThat(ngram, equalTo(words[numNgrams]));
            numNgrams += 1;
        }
        assertThat(numNgrams, equalTo(words.length));
    }
    
    @Test
    @DisplayName("supports trigrams")
    void testTrigrams() { 
        NGramIterator iter = new NGramIterator(doc, TokenForms.lowercase(), 3);
        String[] words = new String[] {"i", "met", "i met", "you", "met you", "i met you", "before", "you before", "met you before", 
                "the", "before the", "you before the", "fall", "the fall", "before the fall", "of", "fall of", "the fall of", "rome", 
                "of rome", "fall of rome", ".", "rome .", "of rome .", "and", "i", "and i", "begged", 
                "i begged", "and i begged", "you", "begged you", "i begged you", "to", "you to", "begged you to", "let", "to let", "you to let", 
                "me", "let me", "to let me", "take", "me take", "let me take", "you", "take you", "me take you", "home", "you home", "take you home", 
                ".", "home .", "you home ."};
        int numNgrams = 0;
        while(iter.hasNext()) {
            String[] next = iter.next();
            String ngram = next.length == 1 ? next[0] : Arrays.stream(next).reduce((a, b) -> a + " " + b).get(); 
            assertThat(ngram, equalTo(words[numNgrams]));
            numNgrams += 1;
        }
        assertThat(numNgrams, equalTo(words.length));
    }

    @Test
    @DisplayName("supports trigrams crossing sentences")
    void testTrigramsCrossSentence() { 
        NGramIterator iter = new NGramIterator(doc, TokenForms.lowercase(), 3, true);
        String[] words = new String[] {"i", "met", "i met", "you", "met you", "i met you", "before", "you before", "met you before", 
                "the", "before the", "you before the", "fall", "the fall", "before the fall", "of", "fall of", "the fall of", "rome", 
                "of rome", "fall of rome", ".", "rome .", "of rome .", "and", ". and", "rome . and", "i", "and i", ". and i", "begged", 
                "i begged", "and i begged", "you", "begged you", "i begged you", "to", "you to", "begged you to", "let", "to let", "you to let", 
                "me", "let me", "to let me", "take", "me take", "let me take", "you", "take you", "me take you", "home", "you home", "take you home", 
                ".", "home .", "you home ."};
        int numNgrams = 0;
        while(iter.hasNext()) {
            String[] next = iter.next();
            String ngram = next.length == 1 ? next[0] : Arrays.stream(next).reduce((a, b) -> a + " " + b).get(); 
            assertThat(ngram, equalTo(words[numNgrams]));
            numNgrams += 1;
        }
        assertThat(numNgrams, equalTo(words.length));
    }
    
    @Test
    @DisplayName("can handle an empty document")
    void testEmpty() { 
        TextDocument text = new TextDocument("test", "");
        StanfordNLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
        AnnotatedTextDocument emptyDoc = pipeline.apply(text);
        NGramIterator iter = new NGramIterator(emptyDoc, TokenForms.lowercase(), 3);
        assertThat(iter.hasNext(), is(false));
    }
}
