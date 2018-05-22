package com.arunge.unmei.iterators.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.arunge.unmei.iterators.CloseableIterator;

/**
 * 
 *<p>A <code>DelimitedLineFileIterator</code> provides a convenient way to read the lines of a file, splitting them and returning {@link DelimitedLine}s
 *   that support retrieving values of particular types using either the index of the column or using the header labels. 
 *
 * @author Andrew Runge
 *
 */
public class DelimitedLineFileIterator implements CloseableIterator<DelimitedLine> {

    private BufferedReader reader;
    private String delimiter;
    private Map<String, Integer> header;
    private String dateFormat;
    private DelimitedLine next;
    
    /**
     * Creates a <code>DelimitedLineFileIterator</code> that reads lines from the given file and splits them
     * using the default delimier (,), treating the first line of the file as the header.
     * @param file The file to read.
     * @throws IOException
     */
    public DelimitedLineFileIterator(File file) throws IOException {
        this(file, ",", null);
    }
    
    /**
     * Creates a <code>DelimitedLineFileIterator</code> that reads lines from the file at the given path and splits them
     * using the default delimier (,), treating the first line of the file as the header.
     * @param path The path pointing to the file to read.
     * @throws IOException
     */
    public DelimitedLineFileIterator(Path path) throws IOException { 
        this(path.toFile(), ",", null);
    }
    
    /**
     * Creates a <code>DelimitedLineFileIterator</code> that reads lines from the file at the given path and splits them
     * using the provided delimiter, treating the first line of the file as the header.
     * @param file The file to read.
     * @param delimiter Delimiter used to split each line of the file.
     * @throws IOException
     */
    public DelimitedLineFileIterator(File file, String delimiter) throws IOException {
        this(file, delimiter, null);
    }
    
    /**
     * Creates a <code>DelimitedLineFileIterator</code> that reads lines from the file at the given path and splits them
     * using the provided delimier, treating the first line of the file as the header.
     * @param path The path pointing to the file to read.
     * @param delimiter Delimiter used to split each line of the file.
     * @throws IOException
     */
    public DelimitedLineFileIterator(Path path, String delimiter) throws IOException {
        this(path.toFile(), delimiter, null);
    }
    
    /**
     * Creates a <code>DelimitedLineFileIterator</code> that reads lines from the file at the given path and splits them
     * using the provided delimiter. <p> 
     * The provided header should use the same delimiter as the specified delimiter. 
     * If header is null, the first line of the file will be treated as the header
     * and subsequently not returned by this iterator.
     * @param path The path pointing to the file to read.
     * @param delimiter Delimiter used to split each line of the file.
     * @param header Manually defined header for the file. Set to null to treat the first line as the header, or set to an empty string to declare no header.
     * @throws IOException 
     */
    public DelimitedLineFileIterator(Path path, String delimiter, String header) throws IOException {
        this(path.toFile(), delimiter, header);
    }
    
    /**
     * Creates a <code>DelimitedLineFileIterator</code> that reads lines from the given file and splits them
     * using the provided delimiter. <p> 
     * The provided header should use the same delimiter as the specified delimiter. 
     * If header is null, the first line of the file will be treated as the header
     * and subsequently not returned by this iterator.
     * @param file The file to read.
     * @param delimiter Delimiter used to split each line of the file.
     * @param header Manually defined header for the file. Set to null to treat the first line as the header, or set to an empty string to declare no header.
     * @throws IOException
     */
    public DelimitedLineFileIterator(File file, String delimiter, String header) throws IOException {
        reader = new BufferedReader(new FileReader(file));
        if(header == null) { 
            header = reader.readLine();
        }
        this.header = new HashMap<>();
        String[] headerKeys = header.split(delimiter);
        for(int i = 0; i < headerKeys.length; i++) {
            this.header.put(headerKeys[i], i);
        }
        this.delimiter = delimiter;
        this.dateFormat = null;
        this.next = loadNext();
    }
    
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    public void setHeader(String header) { 
        this.header = new HashMap<>();
        String[] headerKeys = header.split(delimiter);
        for(int i = 0; i < headerKeys.length; i++) {
            this.header.put(headerKeys[i], i);
        }
    }
    
    private DelimitedLine loadNext() { 
        try {
            String line = reader.readLine();
            if(line == null) { 
                return null;
            }
            String[] lineParts = line.split(delimiter);
            return new DelimitedLine(header, lineParts, dateFormat);
        } catch (IOException e) {
            return null;
        }
        
    }
    
    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public DelimitedLine next() {
        DelimitedLine ret = this.next;
        if(ret == null) { 
            throw new NoSuchElementException("No elements remaining."); 
        }
        this.next = loadNext();
        return ret;
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
    
    
}
