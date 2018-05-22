package com.arunge.unmei.iterators.io;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 
 *<p>class_comment_here<p>
 *
 * @author Andrew Runge
 *
 */
public class DelimitedLine {

    private Map<String, Integer> header;
    private String[] line;
    private SimpleDateFormat dateFormat;
    
    DelimitedLine(Map<String, Integer> header, String[] line, String dateFormat) {
        this.header = header;
        this.line = line;
        if(dateFormat != null) {
            this.dateFormat = new SimpleDateFormat(dateFormat);
        }
    }
    
    public String getString(int index) {
        return line[index];
    }
    
    public String getString(String columnKey) {
        return line[header.get(columnKey)];
    }
    
    public int getInt(int index) {
        return Integer.parseInt(line[index]);
    }
    
    public int getInt(String columnKey) {
        return Integer.parseInt(line[header.get(columnKey)]);
    }
    
    public double getDouble(int index) { 
        return Double.parseDouble(line[index]);
    }
    
    public double getDouble(String columnKey) {
        return Double.parseDouble(line[header.get(columnKey)]);
    }
    
    public float getFloat(int index) {
        return Float.parseFloat(line[index]);
    }
    
    public float getFloat(String columnKey) {
        return Float.parseFloat(line[header.get(columnKey)]);
    }
    
    public long getLong(int index) {
        return Long.parseLong(line[index]);
    }
    
    public long getLong(String columnKey) {
        return Long.parseLong(line[header.get(columnKey)]);
    }
    
    public boolean getBoolean(int index) {
        return Boolean.parseBoolean(line[index]);
    }
    
    public boolean getBoolean(String columnKey) {
        return Boolean.parseBoolean(line[header.get(columnKey)]);
    }
    
    public Date getDate(int index) {
        try { 
            return dateFormat.parse(line[index]);
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse date from string " + line[index]);
        }
    }
    
    public Date getDate(String columnKey) {
        try { 
            return dateFormat.parse(line[header.get(columnKey)]);
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse date from string " + line[header.get(columnKey)]);
        }
    }
    
    public String[] getStringArray(int index, String delimiter) {
        return line[index].split(delimiter);
    }
    
    public String[] getStringArray(String columnKey, String delimiter) {
        return line[header.get(columnKey)].split(delimiter);
    }
    
    public int[] getIntArray(int index, String delimiter) {
        return Arrays.stream(line[index].split(delimiter)).mapToInt(s -> Integer.parseInt(s)).toArray();
    }
    
    public int[] getIntArray(String columnKey, String delimiter) { 
        return Arrays.stream(line[header.get(columnKey)].split(delimiter)).mapToInt(s -> Integer.parseInt(s)).toArray();
    }
    
    public double[] getDoubleArray(int index, String delimiter) { 
        return Arrays.stream(line[index].split(delimiter)).mapToDouble(s -> Integer.parseInt(s)).toArray();
    }
    
    public double[] getDoubleArray(String columnKey, String delimiter) {
        return Arrays.stream(line[header.get(columnKey)].split(delimiter)).mapToDouble(s -> Integer.parseInt(s)).toArray();
    }
    
    public float[] getFloatArray(int index, String delimiter) {
        return toFloatArray(line[index].split(delimiter));
    }
    
    public float[] getFloatArray(String columnKey, String delimiter) { 
        return toFloatArray(line[header.get(columnKey)].split(delimiter));
    }
    
    private float[] toFloatArray(String[] value) {
        float[] ret = new float[value.length];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = Float.parseFloat(value[i]);
        }
        return ret;
    }
    
}
