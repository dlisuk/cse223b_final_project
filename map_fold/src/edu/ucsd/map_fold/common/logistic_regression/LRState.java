package edu.ucsd.map_fold.common.logistic_regression;

import Jama.Matrix;

import java.io.Serializable;

public class LRState implements Serializable{
    public LRState(double _mu, double _lambda, int n) {
        learningRate    = _mu;
        regularization  = _lambda;
        parameters      = new Double[n];
        for( int i = 0; i<n; i ++){parameters[i]=0.0;}
        offset          = 0.0;

    }
    public LRState(double _mu, double _lambda, Double[] _w, double _offset){
        learningRate    = _mu;
        regularization  = _lambda;
        parameters      = _w;
        offset          = _offset;
    }

    public double Apply(Double[] x){
        double pred = offset;
        for(int i = 0; i<x.length; i++){
            pred += x[i] * parameters[i];
        }
        pred = 1.0/(1.0 + Math.exp(-pred));
        return pred;
    }

    public Double getMu()       { return learningRate; }
    public Double getLambda()   { return regularization; }
    public Double getOffset()   { return offset; }
    public Double[] getWeights()  { return parameters; }
    private final Double learningRate;
    private final Double regularization;
    private final Double[] parameters;
    private final Double offset;
}
