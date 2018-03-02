package com.arunge.nlp.api;

import java.io.Serializable;

public class FeatureDescriptor implements Serializable {

    private static final long serialVersionUID = 7406606319368834769L;
    private String name;
    private FeatureWeightType weightType;
    
    public FeatureDescriptor(String name, FeatureWeightType weightType) { 
        this.name = name;
        this.weightType = weightType;
    }

    public static FeatureDescriptor of(String name) {
        return new FeatureDescriptor(name, FeatureWeightType.CONSTANT);
    }
    
    public String getName() {
        return name;
    }

    public FeatureWeightType getWeightType() {
        return weightType;
    }

    public String toString() {
        return name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeatureDescriptor other = (FeatureDescriptor) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
}
