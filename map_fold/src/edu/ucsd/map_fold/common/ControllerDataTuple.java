package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/27/14.
 */
public class ControllerDataTuple {
    public ControllerDataTuple(ControllerInterface ci, boolean primary, boolean liveness){
        this.controllerInterface = ci;
        this.primary = primary;
        this.liveness = liveness;
    }

    public ControllerInterface getControllerInterface(){
        return this.controllerInterface;
    }

    public void setLiveness(boolean liveness) {
        this.liveness = liveness;
    }

    public boolean getLiveness(){
        return this.liveness;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isPrimary() {
        return primary;
    }

    @Override
    public String toString() {
        if(primary){
            return "primary";
        }else{
            return "not primary";
        }

    }

    public ControllerInterface controllerInterface;
    public boolean liveness;
    public boolean primary;
}
