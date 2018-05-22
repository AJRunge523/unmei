package com.arunge.unmei.ml.svm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SVMRank {

    private static Logger LOG = LoggerFactory.getLogger(SVMRank.class);
    
    private String trainPath;
    private String classifyPath;
    
    public SVMRank(Path installDir) {
        this.trainPath = Paths.get(installDir.toAbsolutePath().toString(), "svm_rank_learn.exe").toAbsolutePath().toString();
        this.classifyPath = Paths.get(installDir.toAbsolutePath().toString(), "svm_rank_classify.exe").toAbsolutePath().toString();
    }
    
    public void train(Path trainFile, Path modelFile, String...args) throws IOException {
        List<String> processArgs = new ArrayList<>();
        processArgs.add(trainPath);
        for(String arg : args) {
            processArgs.add(arg);
        }
        processArgs.add(trainFile.toAbsolutePath().toString());
        processArgs.add(modelFile.toAbsolutePath().toString());
        int exitVal = runProcess(processArgs);
        if(exitVal == 0) {
            LOG.info("Finished training SVMRank model.");
        } else {
            LOG.error("Error training SVMRank model. Enable debug logging to see output of process for details.");
        }
        
    }
    
    public List<Double> eval(Path evalFile, Path modelFile, Path outputFile) throws IOException {
        List<String> processArgs = new ArrayList<>();
        processArgs.add(classifyPath);
        processArgs.add(evalFile.toAbsolutePath().toString());
        processArgs.add(modelFile.toAbsolutePath().toString());
        processArgs.add(outputFile.toAbsolutePath().toString());
        int exitVal = runProcess(processArgs);
        if(exitVal == 0) {
            try (BufferedReader reader = new BufferedReader(new FileReader(outputFile.toFile()))) {
                String line = "";
                List<Double> scores = new ArrayList<>();
                while((line = reader.readLine()) != null) {
                    scores.add(Double.parseDouble(line));
                }
                return scores;
            }
        } else {
            LOG.error("Error evaluating SVMRank model. Enable debug logging to see output of process for details.");
            return new ArrayList<>();
        }
        
    }
    
    private int runProcess(List<String> args) throws IOException {
        Process process = new ProcessBuilder(args).start();
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = reader.readLine()) != null) {
            LOG.debug(line);
        }
        return process.exitValue();
    }

    public static String instanceToString(int queryId, int rank, Map<Integer, Double> instance) {
        return instanceToString(queryId, rank, instance, null);
    }
    
    public static String instanceToString(int queryId, int rank, Map<Integer, Double> instance, String info) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(20);
        StringBuffer inst = new StringBuffer().append(rank + " ")
                .append("qid:")
                .append(queryId);
        instance.keySet().stream().sorted().forEach(index -> {
//            System.out.println(instance.get(index));
            inst.append(" ")
                .append(index)
                .append(":")
                .append(df.format(instance.get(index)));
        });
        if(info != null && !info.isEmpty()) {
            inst.append(" # ")
                .append(info);
        }
        return inst.toString();
    }
    
}
