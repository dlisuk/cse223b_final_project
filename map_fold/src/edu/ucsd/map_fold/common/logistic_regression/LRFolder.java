package edu.ucsd.map_fold.common.logistic_regression;

import Jama.Matrix;
import edu.ucsd.map_fold.common.Folder;
import edu.ucsd.map_fold.common.logistic_regression.LRState;

import java.util.Arrays;

public class LRFolder implements Folder<LRState, Double[]> {
    @Override
    public LRState fold(LRState state, Double[] record){
        Double[] w   = state.getWeights();
        Double   mu  = state.getMu();
        Double   lam = state.getLambda();

        Double y     = record[0];
        Double[] x   = Arrays.copyOfRange(record, 1, record.length);
        Double p     = y - state.Apply(x);

        Double[] wPrime = Arrays.copyOf(w,w.length);
        for(int i = 0; i < wPrime.length; i++){
            wPrime[i] += x[i]*p*lam - wPrime[i]*lam*mu;
        }
        double offset = state.getOffset();
        offset += lam*(p-2*mu*offset);
        return new LRState(mu, lam, wPrime, offset);
    }
}
