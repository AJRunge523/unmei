package com.arunge.nlp.features;

import java.util.Map;

import com.arunge.nlp.api.FeatureDescriptor;

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
