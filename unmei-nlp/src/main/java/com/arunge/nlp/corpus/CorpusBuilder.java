package com.arunge.nlp.corpus;

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
import com.arunge.nlp.api.TokenForms;
import com.arunge.nlp.api.TokenForms.TokenForm;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.features.FeatureExtractor;
import com.arunge.nlp.processors.BasicNLPPreprocessingPipeline;
import com.arunge.nlp.processors.PorterStemmerImpl;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.TextDocument;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;
import com.arunge.nlp.tokenization.TokenSplitter;
import com.arunge.nlp.vocab.CountingNGramIndexer;
import com.arunge.nlp.vocab.CountingVocabulary;

public class CorpusBuilder {

    private static Logger LOG = LoggerFactory.getLogger(CorpusBuilder.class);
    
    private Tokenizer tokenizer;
    
    private List<TextSource> sources;
    
    private List<TokenFilter> tokenFilters;
    
    private List<TokenSplitter> tokenSplitters;
    
    private int minDocs;
    
    private int minCount;
    
    private Corpus corpus;
    
    private BasicNLPPreprocessingPipeline pipeline;
    
    private int processedDocs;
    
    private int trimEvery;
    
    private boolean lemmaTag;
    
    private boolean posTag;
    
    private boolean stemTag;
    
    private List<FeatureExtractor<AnnotatedTextDocument>> textFeatureExtractors;
    
    public static CorpusBuilder basicCorpusBuilder() {
        return new CorpusBuilder(new BasicCorpus());
    }
    
    public static CorpusBuilder tfIdfCorpusBuilder() {
        return new CorpusBuilder(new TfIdfCorpus());
    }
    
    public static CorpusBuilder tfIdfCorpusBuilder(TFType type) {
        return new CorpusBuilder(new TfIdfCorpus(type));
    }
    
    public static CorpusBuilder fixedTfIdfCorpusBuilder(CountingVocabulary vocab) {
        return new CorpusBuilder(new FixedVocabTfIdfCorpus(vocab));
    }
    
    public static CorpusBuilder ngramTfIdfCorpusBuilder(int order) {
        return new CorpusBuilder(new TfIdfNgramCorpus(order));
    }
    
    public static CorpusBuilder ngramTfIdfCorpusBuilder(int order, TFType type) {
        return new CorpusBuilder(new TfIdfNgramCorpus(order, type));
    }
    
    public static CorpusBuilder countingVocabCorpusBuilder() {
        return new CorpusBuilder(new CountingCorpus());
    }
    
    public static CorpusBuilder countingVocabCorpusBuilder(CountingVocabulary vocab) {
        return new CorpusBuilder(new CountingCorpus(vocab));
    }
    
    public static CorpusBuilder countingNGramCorpusBuilder(int order, boolean indexOnly) {
        return new CorpusBuilder(new CountingNGramCorpus(order, indexOnly));
    }
    
    public static CorpusBuilder countingNGramCorpusBuilder(CountingNGramIndexer vocab) {
        return new CorpusBuilder(new CountingNGramCorpus(vocab));
    }
    
    public static CorpusBuilder fixedTfIdfNgramCorpusBuilder(File vocabFile, File corpusFile) {
        CountingNGramIndexer vocab;
        try {
            vocab = CountingNGramIndexer.read(vocabFile);
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
        this.tokenFilters = new ArrayList<>();
        this.tokenSplitters = new ArrayList<>();
        this.textFeatureExtractors = new LinkedList<>();
        this.trimEvery = -1;
    }
    
    /**
     * Specify the tokenizer to use.
     * @param tokenizer
     * @return
     */
    public CorpusBuilder withTokenizer(Tokenizer tokenizer) { 
        this.tokenizer = tokenizer;
        return this;
    }
    
    /**
     * Set the {@link TextSource}s to be used for creating the corpus.
     * Overwrites all previously added sources.
     * @param textSources
     * @return
     */
    public CorpusBuilder withSources(Collection<TextSource> textSources) {
        this.sources = new ArrayList<>();
        this.sources.addAll(textSources);
        return this;
    }
    
    /**
     * Add a {@link TextSource} to be used for constructing the corpus.
     * @param source
     * @return
     */
    public CorpusBuilder addSource(TextSource source) { 
        this.sources.add(source);
        return this;
    }
    
    /**
     * Add all of the provided {@link TextSource}s to be used for constructing the corpus.
     * @param textSources
     * @return
     */
    public CorpusBuilder addSources(Collection<TextSource> textSources) { 
        this.sources.addAll(textSources);
        return this;
    }
    
    public CorpusBuilder withMinVocabInclusion(int minCount, int minDocs) {
        this.minDocs = minDocs;
        this.minCount = minCount;
        return this;
    }
    
    public CorpusBuilder withTokenFilter(TokenFilter filter) { 
        this.tokenFilters.add(filter);
        return this;
    }
    
    public CorpusBuilder withTokenFilters(List<TokenFilter> filters) {
        this.tokenFilters.addAll(filters);
        return this;
    }
    
    public CorpusBuilder withTokenSplitter(TokenSplitter splitter) {
        this.tokenSplitters.add(splitter);
        return this;
    }
    
    public CorpusBuilder withTokenSplitters(List<TokenSplitter> splitters) {
        this.tokenSplitters.addAll(splitters);
        return this;
    }
    
    /**
     * Indexes the lowercase forms of the tokens in all documents.
     * @return
     */
    public CorpusBuilder withLowercaseTokenForms() { 
        this.corpus.setTokenFormExtraction(TokenForms.lowercase());
        return this;
    }
    
    /**
     * Indexes the lowercase forms of the tokens, split by the text field they appear in.
     * @return
     */
    public CorpusBuilder withSplitFieldTokenForms() { 
        this.corpus.setTokenFormExtraction(TokenForms.segmented());
        return this;
    }
    
    public CorpusBuilder withLemmaTokenForms() { 
        this.corpus.setTokenFormExtraction(TokenForms.lemma());
        this.posTag = true;
        this.lemmaTag = true;
        return this;
    }
    
    public CorpusBuilder withStemTokenForms() {
        this.corpus.setTokenFormExtraction(TokenForms.stem());
        this.stemTag = true;
        return this;
    }
    
    /**
     * Uses the provided {@link TokenForm} function to extract forms from tokens.
     * Note that no form dependencies are set with this flag (i.e. for lemmatization, pos tagging, etc.)
     * @param form
     * @return
     */
    public CorpusBuilder withTokenForms(TokenForm form) {
        this.corpus.setTokenFormExtraction(form);
        return this;
    }
    
    public CorpusBuilder addTextFeatureExtractor(FeatureExtractor<AnnotatedTextDocument> extractor) { 
        this.textFeatureExtractors.add(extractor);
        return this;
    }
    
    public CorpusBuilder addTextFeatureExtractors(Collection<FeatureExtractor<AnnotatedTextDocument>> extractor) { 
        this.textFeatureExtractors.addAll(extractor);
        return this;
    }
    
    public CorpusBuilder trimEvery(int trimEveryDocs) {
        this.trimEvery = trimEveryDocs;
        return this;
    }
    
    public CorpusBuilder addLemmas() {
        this.posTag = true;
        this.lemmaTag = true;
        return this;
    }
    
    public CorpusBuilder addPOSTags() { 
        this.posTag = true;
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
        if(posTag) {
            annotators.add(Annotator.POS);
        }
        if(lemmaTag) { 
            annotators.add(Annotator.LEMMA);
        }
        Annotator[] anns = annotators.stream().toArray(Annotator[]::new);
        this.pipeline = new StanfordNLPPreprocessingPipeline(anns)
                .withTokenFilters(tokenFilters)
                .withTokenSplitters(tokenSplitters);
        if(stemTag) { 
            this.pipeline = pipeline.withStemmer(new PorterStemmerImpl());
        }
        
//        TextDocumentTokenizer docTokenizer = new TextDocumentTokenizer(tokenizer, tokenFilters);
//        docTokenizer.setTokenSplitters(this.tokenSplitters);
        for(TextSource source : sources) {
            source.getDocuments().forEach(d -> tokenizeAndAdd(d));
            LOG.info("Finished processing text source");
        }
        if(minDocs > 0 || minCount > 0) {
            corpus.trimTail(minCount, minDocs);
        }
        corpus.finalize();
        return corpus;
    }
    
    /**
     * Tokenize the document and add it to the corpus.
     * @param vocab
     * @param doc
     */
    private void tokenizeAndAdd(TextDocument doc) {
        AnnotatedTextDocument processed = pipeline.apply(doc);
//        processed = docTokenizer.splitTokens(processed);
//        processed = docTokenizer.filter(processed);
        for(FeatureExtractor<AnnotatedTextDocument> extractor : textFeatureExtractors) { 
            processed.addFeatures(extractor.extractFeatures(processed));
        }
        corpus.addTokenizedDocument(processed);
        processedDocs += 1;
        if(processedDocs % 500 == 0) { 
            LOG.info("Processed {} documents. Vocabulary size: {}", processedDocs, corpus.getVocabulary().size());
        }
        if(trimEvery > 0 && processedDocs % trimEvery == 0) { 
            corpus.trimTail(minCount, minDocs);
        }
    }
    
}
