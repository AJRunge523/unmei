package com.arunge.unmei.ml.svm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    }
    
}
