package com.arunge.nlp.api;

public class NgramKeyCompression {

    public static long generateKey(int[] ngram, int from, int to) {
        if(to - from == 2) {
            return generateBigramKey(ngram[from], ngram[from + 1]);
        } else if(to - from == 3) {
            return generateTrigramKey(ngram[from], ngram[from + 1], ngram[from + 2]);
        } else {
            return -1;
        }
    }
    
    public static long generateKey(int[] ngram) {
        if(ngram.length == 2) {
            return generateBigramKey(ngram[0], ngram[1]);
        } else if(ngram.length == 3) { 
            return generateTrigramKey(ngram[0], ngram[1], ngram[2]);
        } else {
            return -1;
        }
    }
    
    public static int[] extractKey(long key, int order) {
        if(order == 2) {
            return extractBigramKey(key);
        } else if(order == 3) {
            return extractTrigramKey(key);
        } else {
            return new int[0];
        }
    }
    
    /**
     * 
     * @param curr
     * @param prev
     * @return
     */
    public static long generateBigramKey(int prev, int curr) {
        long key = 0;
        key |= prev;
        key = key << 32;
        long ret = key | curr;
        return ret;
    }
    
    public static int[] extractBigramKey(long key) {
        int prev = (int) (key >> 32);
        int curr = (int) key;
        return new int[] {prev, curr};
    }
    
    /**
     * 
     * @param curr The current word index
     * @param prev The previous (n-1) word index
     * @param prevPrev The n-2 word index
     * @return
     */
    public static long generateTrigramKey(int prevPrev, int prev, int curr) {
        long compressed = 0;
        compressed = (compressed | prevPrev) << 20;
        compressed = (compressed | prev) << 20;
        compressed = (compressed | curr);
        return compressed;
    }
    
    public static int[] extractTrigramKey(long key) {
        int curr = (int) ((key << 44) >> 44);
        int prev = (int) ((key << 24) >> 44);
        int prevPrev = (int) (key >> 40);
        return new int[] {prevPrev, prev, curr};
    }
    
}
