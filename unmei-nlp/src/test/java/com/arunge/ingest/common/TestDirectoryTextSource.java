package com.arunge.ingest.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arunge.nlp.text.TextDocument;

public class TestDirectoryTextSource {

    private static Path testDir;
    
    @BeforeClass
    public static void setup() throws IOException { 
        testDir = Files.createTempDirectory(Paths.get("src/test/resources/"), "tst");
    }
    
    @AfterClass
    public static void teardown() throws IOException {
        Files.delete(testDir);
    }
    
    @After
    public void clearFiles() throws IOException {
        for(File f : testDir.toFile().listFiles()) {
            Files.delete(Paths.get(f.getAbsolutePath()));
        }
    }
    
    @Test
    public void testDirectorySource() {
        try {
            Set<String> texts = new HashSet<>();
            texts.add("This is a test sentence for file 1.");
            texts.add("Test document 3.");
            texts.add("");
            for(String s : texts) {
                createTempFile(s, ".txt");
            }
            createTempFile("This is a test sentence for file 2.\nThere are multiple lines in this file.", ".rtf");
            texts.add("This is a test sentence for file 2.\nThere are multiple lines in this file.");
            DirectoryTextSource source = new DirectoryTextSource(testDir.toFile());
            List<String> docs = source.getDocuments().map(doc -> doc.getText()).collect(Collectors.toList());
            assertEquals(docs.size(), texts.size());
            for(String s : docs) {
                assertTrue(texts.contains(s));
            }
        } catch (IOException e) { 
            fail("Threw exception: " + e.getMessage());
        }
        
    }
    
    @Test
    public void testEmptyDirectory() {
        DirectoryTextSource source = new DirectoryTextSource(testDir.toFile());
        Stream<TextDocument> docs = source.getDocuments();
        assertEquals(0, docs.count());
    }
    
    @Test
    public void testFilteredSource() {
        try {
            Set<String> texts = new HashSet<>();
            texts.add("This is a test sentence for file 1.");
            texts.add("Test document 3.");
            texts.add("");
            for(String s : texts) {
                createTempFile(s, ".txt");
            }
            createTempFile("This is a test sentence for file 2.\nThere are multiple lines in this file.", ".rtf");
            DirectoryTextSource source = new DirectoryTextSource(testDir.toFile(), "txt");
            List<String> docs = source.getDocuments().map(doc -> doc.getText()).collect(Collectors.toList());
            assertEquals(docs.size(), texts.size());
            for(String s : docs) {
                assertTrue(texts.contains(s));
            }
            
        } catch (IOException e) { 
            fail("Threw exception: " + e.getMessage());
        }
    }
    
    private void createTempFile(String content, String suffix) throws IOException {
        Path temp = Files.createTempFile(testDir, "tst-", suffix);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(temp.toFile()))){ 
            writer.write(content);
        }
    }
    
}
