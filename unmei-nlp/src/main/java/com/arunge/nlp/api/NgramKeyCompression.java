package com.arunge.nlp.api;

public class NgramKeyCompression {

    private static int BIGRAM_KEY_LIMIT = (int) Math.pow(2, 30);
    private static int TRIGRAM_KEY_LIMIT = (int) Math.pow(2,  20);
    
    public static long generateKey(int[] ngram, int from, int to) {
        if(to - from == 1) {
            return (long) ngram[from];
        } else if(to - from == 2) {
            return generateBigramKey(ngram[from], ngram[from + 1]);
        } else if(to - from == 3) {
            return generateTrigramKey(ngram[from], ngram[from + 1], ngram[from + 2]);
        } else {
            return -1;
        }
    }
    
    public static long generateKey(int[] ngram) {
        if(ngram.length == 1) {
            return (long) ngram[0];
        } else if(ngram.length == 2) {
            return generateBigramKey(ngram[0], ngram[1]);
        } else if(ngram.length == 3) { 
            return generateTrigramKey(ngram[0], ngram[1], ngram[2]);
        } else {
            return -1;
        }
    }
    
    public static int[] extractKey(long key) {
        int order = extractOrder(key);
        if(order == 1) { 
            return new int[] {(int) key};
        } else if(order == 2) {
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
        if(prev >= BIGRAM_KEY_LIMIT || curr >= BIGRAM_KEY_LIMIT) {
            throw new UnsupportedOperationException("Cannot create bigram keys with vocabulary items with indices higher than " + (BIGRAM_KEY_LIMIT - 1));
        }
        long key = 1;
        key = key << 30;
        key |= prev;
        key = key << 32;
        long ret = key | curr;
        return ret;
    }
    
    public static int[] extractBigramKey(long key) {
        key = key << 2 >>> 2;
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
        if(prevPrev >= TRIGRAM_KEY_LIMIT || prev >= TRIGRAM_KEY_LIMIT || curr >= TRIGRAM_KEY_LIMIT) {
            throw new UnsupportedOperationException("Cannot create trigram keys with vocabulary items with indices higher than " + (TRIGRAM_KEY_LIMIT - 1));
        }
        long compressed = 2;
        compressed = compressed << 22;
        compressed = (compressed | prevPrev) << 20;
        compressed = (compressed | prev) << 20;
        compressed = (compressed | curr);
        return compressed;
    }
    
    public static int[] extractTrigramKey(long key) {
        key = key << 2 >>> 2;
        int curr = (int) ((key << 44) >>> 44);
        int prev = (int) ((key << 24) >>> 44);
        int prevPrev = (int) (key >>> 40);
        return new int[] {prevPrev, prev, curr};
    }
    
    private static int extractOrder(long key) {
        return (int) (key >>> 62) + 1;
    }
    
}
