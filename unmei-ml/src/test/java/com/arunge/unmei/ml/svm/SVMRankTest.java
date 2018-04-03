package com.arunge.unmei.ml.svm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class SVMRankTest {
    
    @Test
    public void testSVMRank() throws Exception {
        SVMRank rank = new SVMRank(Paths.get("J:\\Program Files\\SVMRank"));
        Path trainFile = Paths.get("src/test/resources/svmrank/train.dat");
        Path evalFile = Paths.get("src/test/resources/svmrank/test.dat");
        Path modelFile = Paths.get("src/test/resources/svmrank/model.model");
        Path outputFile = Paths.get("src/test/resources/svmrank/test.out");
        rank.train(trainFile, modelFile, "-c", "20");
        
        List<Double> scores = rank.eval(evalFile, modelFile, outputFile);
        assertEquals(scores.size(), 4);
        for(int i = 1; i < scores.size(); i++) {
            assertTrue(scores.get(i - 1) > scores.get(i));
        }
        Files.delete(modelFile);
        Files.delete(outputFile);
    }
    
    @Test
    public void testInstanceConversion() {
        Map<Integer, Double> inst1 = new HashMap<>();
        inst1.put(1, 0.7);
        inst1.put(2, 0.3);
        inst1.put(4, 0.0000001);
        String instance1 = SVMRank.instanceToString(1, 2, inst1);
        assertEquals(instance1, "2 qid:1 1:0.7 2:0.3 4:0.0000001");
    }
        
    
}
