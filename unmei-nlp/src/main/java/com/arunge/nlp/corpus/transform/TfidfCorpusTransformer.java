package com.arunge.nlp.corpus.transform;

import java.util.Map;

import com.arunge.nlp.corpus.Corpus;
import com.arunge.nlp.corpus.CorpusDocument;
import com.arunge.nlp.corpus.CorpusTransformer;
import com.arunge.nlp.corpus.CountingNGramCorpus;
import com.arunge.nlp.corpus.NGramCorpus;
import com.arunge.nlp.vocab.CountingNGramIndexer;
import com.arunge.nlp.vocab.NGramIndexer;

/**
 * 
 *<p>A <code>CorpusTransformer</code> that weights the terms in all documents
 *   of a corpus using the TF-IDF measure. Document frequencies can be taken from pre-computed
 *   values in the corpus or can be computed dynamically from the documents
 *   in the corpus. This transformer can also be used to transform single documents
 *   if {@link TfidfCorpusTransformer#TfidfCorpusTransformer(TFType tfType, double[] idfWeights)} is used
 *   to provide the inverse document frequency weights.<p>
 *
 * @author Andrew Runge
 *
 */
public class TfidfCorpusTransformer implements CorpusTransformer {

    private TFType tfType;
    private NormType normType;
    private double[] idfWeights;
    
    /**
     * Initializes a TfidfCorpusTransformer, using the provided {@link TFType}
     * to determine how to compute the term frequency values.
     * 
     * Note that the transformer can only transform a corpus when initialized
     * with this constructor.
     * @param tfType
     */
    public TfidfCorpusTransformer(TFType tfType, NormType normType) { 
        this.tfType = tfType;
        this.normType = normType;
    }

    /**
     * Initializes a TfidfCorpusTransformer using the provided {@link TFType}
     * to determine how to compute the term frequency values and using
     * the provided idfWeights as the inverse document frequency values.
     * Providing these weights allows the transformer to also transform
     * individual {@link CorpusDocument}s, as well as a whole corpus.
     * 
     * @param tfType
     * @param indexer
     */
    public TfidfCorpusTransformer(TFType tfType, double[] idfWeights) { 
        this.tfType = tfType;
        this.idfWeights = idfWeights;
    }
    
    @Override
    public void transform(Corpus corpus) {
        // TODO Auto-generated method stub
        double[] idfWeights = null;
        if(corpus instanceof CountingNGramCorpus) {
            CountingNGramCorpus countCorpus = (CountingNGramCorpus) corpus;
            CountingNGramIndexer indexer = (CountingNGramIndexer) countCorpus.getNgramIndexer();
            idfWeights = indexer.computeIDFVector();
        } else if(corpus instanceof NGramCorpus) {
            NGramIndexer indexer = ((CountingNGramCorpus) corpus).getNgramIndexer();
            idfWeights = new double[indexer.size()];
            for(CorpusDocument doc : corpus) {
                for(int o = 1; o <= doc.getOrder(); o++) {
                    Map<Integer, Double> ngramCounts = doc.getNgrams(o);
                    for(int ngramIdx : ngramCounts.keySet()) {
                        idfWeights[ngramIdx] += 1;
                    }
                }
            }
            for(int i = 0; i < idfWeights.length; i++) {
                idfWeights[i] = Math.log(corpus.size() / idfWeights[i]);
            }
        } else {
            throw new UnsupportedOperationException("Currently no support for computing tfidf counts for a corpus of class " + corpus.getClass().getName());
        }
        for(CorpusDocument doc : corpus) {
            transform(doc, idfWeights);

        }
    }

    @Override
    public void transform(CorpusDocument doc) {
        transform(doc, idfWeights);
    }
    
    private void transform(CorpusDocument doc, double[] idfWeights) { 
        double vecLength = 0.0;
        for(int o = 1; o <= doc.getOrder(); o++) {
            double length = doc.getNgramLength(o);
            Map<Integer, Double> ngramCounts = doc.getNgrams(o);
            for(int key : ngramCounts.keySet()) {
                double tfValue = ngramCounts.get(key);
                switch(tfType) {
                case LENGTH_NORM:
                    tfValue /= length;
                    break;
                case LOG_LENGTH_NORM:
                    tfValue = 1 + Math.log(tfValue / length);
                    break;
                case RAW:
                    break;
                default:
                    break; 
                }
                double tfidf = tfValue * idfWeights[key];
                switch(normType) {
                case L1:
                    vecLength += tfidf;
                    break;
                case L2:
                    vecLength += tfidf * tfidf;
                    break;
                case NONE:
                    break;
                default:
                    break;
                
                }
                doc.setNgramValue(key, o, tfidf);
            }
        }
        if(vecLength > 0) { 
            if(normType == NormType.L2) {
                vecLength = Math.sqrt(vecLength);
            }
            for(int o = 1; o <= doc.getOrder(); o++) {
                Map<Integer, Double> ngramCounts = doc.getNgrams(o);
                for(int key : ngramCounts.keySet()) {
                    doc.setNgramValue(key, o, doc.getNgramValue(key, o) / vecLength);
                }
            }
        }
        
        
    }
    
}
