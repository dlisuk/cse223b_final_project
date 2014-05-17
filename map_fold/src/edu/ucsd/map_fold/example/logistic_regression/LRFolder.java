package edu.ucsd.map_fold.example.logistic_regression;

import Jama.Matrix;
import edu.ucsd.map_fold.common.Folder;

public class LRFolder implements Folder<LRState, Matrix> {
    @Override
    public LRState fold(LRState state, Matrix record) {
        Matrix w   = state.getWeights();
        double mu  = state.getMu();
        double lam = state.getLambda();

        double y   = record.get(0,0);
        Matrix x   = record.getMatrix(0, 0, 1, record.getColumnDimension());
        double p   = state.Apply(x);

        Matrix wPrime = x.times(y - p).transpose().times(lam).plusEquals(w.times(1 - 2 * lam * mu));
        double offset = state.getOffset();
        offset += lam*(y-p-2*mu*offset);
        return new LRState(mu, lam, wPrime, offset);
    }
}
