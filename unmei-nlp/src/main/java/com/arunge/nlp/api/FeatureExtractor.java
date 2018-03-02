package com.arunge.nlp.api;

import java.util.Map;

/**
 * 
 *<p>Interface for extracting one or more real-valued features from an input.<p>
 *
 * @author Andrew Runge
 *
 * @param <T>
 */
public interface FeatureExtractor<T> {

    Map<FeatureDescriptor, Double> extractFeatures(T input);
}
