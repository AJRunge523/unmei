package com.arunge.nlp.vocab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.ingest.TextSource;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.Corpus;
import com.arunge.nlp.api.FeatureExtractor;
import com.arunge.nlp.api.TokenFilters;
import com.arunge.nlp.api.TokenFilters.TokenFilter;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.TextDocument;
import com.arunge.nlp.text.TextDocumentTokenizer;

public class CorpusBuilder {

    private static Logger LOG = LoggerFactory.getLogger(CorpusBuilder.class);
    
    private Tokenizer tokenizer;
    
    private List<TextSource> sources;
    
    private List<TokenFilter> tokenFilters;
    
    private int minInclusion;
    
    private Corpus corpus;
    
    private boolean splitTokens;
    
    private StanfordNLPPreprocessingPipeline pipeline;
    
    private int processedDocs;
    
    private List<FeatureExtractor<PreprocessedTextDocument>> textFeatureExtractors;
    
    public static CorpusBuilder basicCorpusBuilder() {
        return new CorpusBuilder(new BasicCorpus());
    }
    
    public static CorpusBuilder tfIdfCorpusBuilder() {
        return new CorpusBuilder(new TfIdfCorpus());
    }
    
    public static CorpusBuilder tfIdfCorpusBuilder(TFType type) {
        return new CorpusBuilder(new TfIdfCorpus(type));
    }
    
    public static CorpusBuilder fixedTfIdfCorpusBuilder(DFVocabulary vocab) {
        return new CorpusBuilder(new FixedVocabTfIdfCorpus(vocab));
    }
    
    public static CorpusBuilder ngramTfIdfCorpusBuilder(int order) {
        return new CorpusBuilder(new TfIdfNgramCorpus(order));
    }
    
    public static CorpusBuilder ngramTfIdfCorpusBuilder(int order, TFType type) {
        return new CorpusBuilder(new TfIdfNgramCorpus(order, type));
    }
    
    public static CorpusBuilder fixedTfIdfNgramCorpusBuilder(File vocabFile, File corpusFile) {
        DFNGramIndexer vocab;
        try {
            vocab = DFNGramIndexer.read(vocabFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load ngram vocabulary from file " + vocabFile.getAbsolutePath(), e);
        }
        try {
            TfIdfNgramCorpus corpus = (TfIdfNgramCorpus) Corpus.loadCorpus(corpusFile);
            return new CorpusBuilder(new FixedTfIdfNgramCorpus(vocab, corpus.getFeatures()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load corpus from file " + corpusFile.getAbsolutePath(), e);
        }
    }
    
    private CorpusBuilder(Corpus corpus) {
        this.sources = new ArrayList<>();
        this.corpus = corpus;
        this.tokenizer = Tokenizers.getDefault();
        this.tokenFilters = TokenFilters.getDefaultFilters();
        this.textFeatureExtractors = new LinkedList<>();
    }
    
    public CorpusBuilder withTokenizer(Tokenizer tokenizer) { 
        this.tokenizer = tokenizer;
        return this;
    }
    
    public CorpusBuilder withSources(Collection<TextSource> textSources) {
        this.sources = new ArrayList<>();
        this.sources.addAll(textSources);
        return this;
    }
    
    public CorpusBuilder addSource(TextSource source) { 
        this.sources.add(source);
        return this;
    }
    
    public CorpusBuilder addSources(Collection<TextSource> textSources) { 
        this.sources.addAll(textSources);
        return this;
    }
    
    public CorpusBuilder withMinVocabInclusion(int minInclusion) {
        this.minInclusion = minInclusion;
        return this;
    }
    
    public CorpusBuilder withTokenFilter(TokenFilter filter) { 
        this.tokenFilters.add(filter);
        return this;
    }
    
    public CorpusBuilder splitTokensByTextField(boolean splitTokens) {
        this.splitTokens = splitTokens;
        return this;
    }
    
    public CorpusBuilder addTextFeatureExtractor(FeatureExtractor<PreprocessedTextDocument> extractor) { 
        this.textFeatureExtractors.add(extractor);
        return this;
    }
    
    public CorpusBuilder addTextFeatureExtractors(Collection<FeatureExtractor<PreprocessedTextDocument>> extractor) { 
        this.textFeatureExtractors.addAll(extractor);
        return this;
    }
    
    /**
     * Constructs a corpus from the documents provided by the one or more {@link TextSource}s.
     * 
     * @return
     */
    public Corpus build() {
        if(sources.size() == 0) { 
            throw new UnsupportedOperationException("Cannot build a corpus without at least one TextSource.");
        }
        List<Annotator> annotators = new ArrayList<>();
//        annotators.add(Annotator.POS);
//        annotators.add(Annotator.LEMMA);
        if(splitTokens) {
            annotators.add(Annotator.SEGMENT);
        }
        Annotator[] anns = annotators.stream().toArray(Annotator[]::new);
        this.pipeline = new StanfordNLPPreprocessingPipeline(anns);
        TextDocumentTokenizer docTokenizer = new TextDocumentTokenizer(tokenizer, tokenFilters, splitTokens);
        for(TextSource source : sources) {
            source.getDocuments().forEach(d -> tokenizeAndAdd(docTokenizer, d));
            LOG.info("Finished processing text source");
        }
        if(minInclusion > 0) {
            corpus.trimTail(minInclusion);
        }
        corpus.finalize();
        return corpus;
    }
    
    /**
     * Tokenize the document and add it to the corpus.
     * @param vocab
     * @param doc
     */
    private void tokenizeAndAdd(TextDocumentTokenizer docTokenizer, TextDocument doc) {
        PreprocessedTextDocument processed = pipeline.apply(doc);
        processed = docTokenizer.filter(processed);
        for(FeatureExtractor<PreprocessedTextDocument> extractor : textFeatureExtractors) { 
            processed.addFeatures(extractor.extractFeatures(processed));
        }
        corpus.addTokenizedDocument(processed);
        processedDocs += 1;
        if(processedDocs % 500 == 0) { 
            LOG.info("Processed {} documents. Vocabulary size: {}", processedDocs, corpus.getVocabulary().size());
        }
    }
    
}
