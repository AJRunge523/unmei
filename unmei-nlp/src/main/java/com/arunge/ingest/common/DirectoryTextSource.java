package com.arunge.ingest.common;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import com.arunge.ingest.TextSource;
import com.arunge.nlp.text.TextDocument;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

/**
 * 
 *<p>A {@link TextSource} that loads documents from a directory. Each document in the directory <p>
 *
 * @author Andrew Runge
 *
 */
public class DirectoryTextSource implements TextSource {

    private File textDir;
    private String typeSuffix;
    
    public DirectoryTextSource(File textDir) {
        this(textDir, "");
    }
    
    public DirectoryTextSource(File textDir, String typeSuffix) { 
        this.textDir = textDir;
        this.typeSuffix = typeSuffix;
    }
    
    @Override
    public Stream<TextDocument> getDocuments() {
        Stream<File> fileList = Arrays.stream(textDir.listFiles());
        return fileList.filter(f -> matchesType(f))
                .map(f -> loadText(f))
                .filter(f -> f.isPresent())
                .map(f -> f.get());
    }

    private boolean matchesType(File f) {
        return typeSuffix.isEmpty() || f.getName().endsWith(typeSuffix);
    }
    
    private Optional<TextDocument> loadText(File file) {
        try {
            String content = Files.asCharSource(file, Charsets.UTF_8).read();
            return Optional.of(new TextDocument(file.getName(), content));
        } catch (IOException e) { 
            return Optional.empty();
        }
    }
    
}
