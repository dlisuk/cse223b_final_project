package edu.ucsd.map_fold.common.logistic_regression;

import Jama.Matrix;

public class LRState {
    public LRState(double _mu, double _lambda, int n) {
        learningRate    = _mu;
        regularization  = _lambda;
        parameters      = new Matrix(1,n);
        offset          = 0.0;

    }
    public LRState(double _mu, double _lambda, Matrix _w, double _offset){
        learningRate    = _mu;
        regularization  = _lambda;
        parameters      = _w;
        offset          = _offset;
    }

    public double Apply(Matrix x){
        double pred = offset;
        pred += parameters.times(x).get(0,0);
        pred = 1.0/(1.0 + Math.exp(-pred));
        return pred;
    }

    public double getMu()       { return learningRate; }
    public double getLambda()   { return regularization; }
    public double getOffset()   { return offset; }
    public Matrix getWeights()  { return parameters; }
    private final double learningRate;
    private final double regularization;
    private final Matrix parameters;
    private final double offset;
}
