package com.arunge.nlp.weka;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.corpus.Corpus;
import com.arunge.nlp.corpus.CorpusDocument;
import com.arunge.nlp.corpus.TfIdfNgramCorpus;
import com.arunge.nlp.vocab.CountingNGramIndexer;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibLINEAR;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

public class WekaCorpusModelBuilder {

    public static enum CorpusType {
        NGRAM,
        ARFF
    }
    
    public static enum SVMKernelType {
        LINEAR,
        POLYNOMIAL,
        RADIAL
    }
    
    private static Logger LOG = LoggerFactory.getLogger(WekaCorpusModelBuilder.class);
    
    private File vocabFile;
    private File corpusFile;
    private Instances instances;
    private CorpusType type;
    private File arffFile;
    private boolean includeIds;
    
    
    public WekaCorpusModelBuilder(File vocabFile, File corpusFile, CorpusType type, boolean includeIds) {
        this.vocabFile = vocabFile;
        this.corpusFile = corpusFile;
        this.instances = null;
        this.type = type;
        this.includeIds = includeIds;
    }
    
    public WekaCorpusModelBuilder(File arffFile, boolean includeIds) {
        this.arffFile = arffFile;
        this.type = CorpusType.ARFF;
        this.instances = null;
        this.includeIds = includeIds;
    }
    
    public Classifier trainClassifier(WekaModelType modelType) throws Exception {
        switch(modelType) {
        case LIBSVM_LINEAR:
            return trainSVM(SVMKernelType.LINEAR);
        case LIBLIN_PRIMAL:
            return trainLibLinear();
        case LIBSVM_POLYNOMIAL:
            return trainSVM(SVMKernelType.POLYNOMIAL);
        case LIBSVM_RBF:
            return trainSVM(SVMKernelType.RADIAL);
        case NAIVE_BAYES:
            return trainNaiveBayes();
        case J48:
            return trainJ48();
        default:
            throw new RuntimeException("Unrecognized weka model type");
        }
    }
    
    private Classifier trainClassifier(Classifier classifier) throws Exception {
        if(instances == null) {
            instances = loadInstances();
        }
        classifier.buildClassifier(instances);
        return classifier;
    }
    
    public Classifier trainNaiveBayes() throws Exception {
        if(instances == null) {
            instances = loadInstances();
        }
        NaiveBayes classifier = new NaiveBayes();
        LOG.info("Training Naive Bayes classifier");
        if(includeIds) { 
            Remove rm = new Remove();
            //The remove filter indexes attributes starting at 1, so really this removes the attribute label @ classIndex - 1
            rm.setAttributeIndices("" + (instances.classIndex()));
            FilteredClassifier filtered = new FilteredClassifier();
            filtered.setFilter(rm);
            filtered.setClassifier(classifier);
            return trainClassifier(filtered);
        }
        return trainClassifier(classifier);
        
    }
    
    public Classifier trainJ48() throws Exception {
        if(instances == null) {
            instances = loadInstances();
        }
        String[] options = new String[]{"-C", "0.25", "-M", "5", "-doNotMakeSplitPointActualValue"};
        J48 classifier = new J48();
        classifier.setOptions(options);
        LOG.info("Training J48 classifier");
        Remove rm = new Remove();
        //The remove filter indexes attributes starting at 1, so really this removes the attribute label @ classIndex - 1
        rm.setAttributeIndices("" + (instances.classIndex()));
        FilteredClassifier filtered = new FilteredClassifier();
        filtered.setFilter(rm);
        filtered.setClassifier(classifier);
        return trainClassifier(filtered);
    }
    
    
    
    public Classifier trainSVM(SVMKernelType kernelType) throws Exception {
        if(instances == null) {
            instances = loadInstances();
        }
        String[] options = null;
        switch(kernelType) {
        case LINEAR:
            options = new String[] {"-S", "0", "-K", "0", "-D", "2", "-G", "0.0", "-R", "0.0", "-N", "0.5", "-M", "40.0", "-C", "1000.0", "-E", "0.001", "-P", "0.1", "-B"};
            break;
        case POLYNOMIAL:
            options = new String[] {"-S", "0", "-K", "1", "-D", "2", "-G", "1", "-R", "0.0", "-N", "0.5", "-M", "40.0", "-C", "10000.0", "-E", "0.001", "-P", "0.1", "-B"};
            break;
        case RADIAL:
            options = new String[] {"-S", "0", "-K", "2", "-D", "2", "-G", "0.1", "-R", "0.0", "-N", "0.5", "-M", "40.0", "-C", "1000.0", "-E", "0.001", "-P", "0.1", "-B"};
            break;
        }
        LibSVM svm = new LibSVM();
        svm.setOptions(options);
        LOG.info("Training SVM classifier with {} kernel", kernelType.name());
        if(includeIds) { 
            Remove rm = new Remove();
            //The remove filter indexes attributes starting at 1, so really this removes the attribute label @ classIndex - 1
            rm.setAttributeIndices("" + (instances.classIndex()));
            FilteredClassifier filtered = new FilteredClassifier();
            filtered.setFilter(rm);
            filtered.setClassifier(svm);
            return trainClassifier(filtered);
        }
        return trainClassifier(svm);
    }
    
    public Classifier trainLibLinear() throws Exception {
        if(instances == null) {
            instances = loadInstances();
        }
        LibLINEAR libLinear = new LibLINEAR();
        String[] options = new String[] {"-S", "0", "-C", "1000.0", "-E", "0.001", "-B", "1.0", "-P", "-L", "0.1", "-I", "1000"};
        libLinear.setOptions(options);
        LOG.info("Training LibLinear SVM classifier");
        if(includeIds) { 
            Remove rm = new Remove();
            //The remove filter indexes attributes starting at 1, so really this removes the attribute label @ classIndex - 1
            rm.setAttributeIndices("" + (instances.classIndex()));
            FilteredClassifier filtered = new FilteredClassifier();
            filtered.setFilter(rm);
            filtered.setClassifier(libLinear);
            return trainClassifier(filtered);
        }
        return trainClassifier(libLinear);
    }
    
    public static Classifier loadClassifier(String path) throws Exception {
        return (Classifier) SerializationHelper.read(path);
    }
    
    public static Classifier loadClassifier(File file) throws Exception {
        return (Classifier) SerializationHelper.read(new FileInputStream(file));
    }
    
    public void saveClassifier(Classifier classifier, String path) throws Exception {
        SerializationHelper.write(path, classifier);
    }
    
    public void saveInstances(File arffOutput) throws IOException {
        if(instances == null) {
            instances = loadInstances();
        }
        LOG.info("Saving arff file");
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        saver.setFile(arffOutput);
        saver.writeBatch();
    }
    
    public void saveInstances(String arffOutput) throws IOException { 
        saveInstances(new File(arffOutput));
    }
    
    private Instances loadInstances() throws IOException {
        if(instances == null) {
            switch(type) { 
            case NGRAM:
                LOG.info("Loading instances from corpus files...");
                this.instances = buildInstancesFromNgramCorpus();
                break;
            case ARFF:
                LOG.info("Loading pre-built instances");
                this.instances = loadArffInstances();
            default:
                break;
            }
        }
        return instances;
    }
    
    private Instances loadArffInstances() throws IOException {
        try {
            DataSource source = new DataSource(arffFile.getAbsolutePath());
            System.out.println(arffFile.getAbsolutePath());
            Instances inst = source.getDataSet();
            if(inst.classIndex() == -1) {
                inst.setClassIndex(inst.numAttributes() - 1);
            }
            return inst;
        } catch (Exception e) {
            LOG.error("Error loading data from arff file", e);
            throw new RuntimeException(e);
        }
    }
    
    private Instances buildInstancesFromNgramCorpus() throws IOException {
        CountingNGramIndexer indexer = CountingNGramIndexer.read(vocabFile);
        ArrayList<Attribute> ngramAttrs = WekaCorpusConverters.createNgramVocabAttributes(indexer);
        ArrayList<Attribute> attributes = new ArrayList<>(ngramAttrs);
        int totalVocabAttrs = attributes.size();
        TfIdfNgramCorpus corpus = (TfIdfNgramCorpus) Corpus.loadCorpus(corpusFile);
        for(Map.Entry<FeatureDescriptor, Integer> entry : corpus.getFeatures()) {
            attributes.add(new Attribute(entry.getKey().getName()));
        }
        ArrayList<String> nullList = null;
        if(includeIds) {
            Attribute idAttr = new Attribute("__id", nullList);
            attributes.add(idAttr);
        }
        Attribute classAttr = new Attribute("patent_class_label", new ArrayList<>(corpus.getClassLabels()));
        attributes.add(classAttr);
        Instances instances = new Instances("Corpus", attributes, corpus.size());
        instances.setClassIndex(attributes.size() - 1);
        int docsRead = 0;
        for(CorpusDocument doc : corpus) {
            instances.add(WekaCorpusConverters.convert((CorpusDocument) doc, totalVocabAttrs, indexer, attributes, includeIds));
            docsRead += 1;
            if(docsRead % 1000 == 0) { 
                LOG.info("Processed {} corpus entries.", docsRead);
            }
        }
        LOG.info("Finished loading ngram vocabulary with {} entries.", attributes.size());
        return instances;
    }
    

    
}
