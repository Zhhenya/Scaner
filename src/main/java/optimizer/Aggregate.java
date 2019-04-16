package optimizer;

import triad.Reference;
import triad.Reference.*;

import java.util.ArrayList;
import java.util.List;

public abstract class Aggregate {
    
    public final List<Reference> left = new ArrayList<>(), right = new ArrayList<>();
    public final List<Integer> triads = new ArrayList<>();
    
    public abstract Aggregate copy();
    
    protected Aggregate copyValues(Aggregate aggregate) {
        left.addAll(aggregate.left);
        right.addAll(aggregate.right);
        triads.addAll(aggregate.triads);
        return this;
    }
    
    @SuppressWarnings("StatementWithEmptyBody")
    public void remove(Reference ref) {
        while(left.remove(ref));
        while(right.remove(ref));
    }
    
    public void removeGlobalVariables() {
        removeGlobalVariables(left);
        removeGlobalVariables(right);
    }
    
    private void removeGlobalVariables(List<Reference> list) {
        list.removeIf((r) -> r instanceof VariableReference && ((VariableReference)r).variable.global);
    }
    
    public int size() {
        return left.size() + right.size();
    }
    
    public static class MultiplicationAggregate extends Aggregate {
        
        @Override
        public Aggregate copy() {
            return new MultiplicationAggregate().copyValues(this);
        }
        
        @Override
        public String toString() {
            return left.toString() + " / " + right.toString() + " | " + triads;
        }
        
    }
    
    public static class AdditionAggregate extends Aggregate {

        @Override
        public Aggregate copy() {
            return new AdditionAggregate().copyValues(this);
        }
        
        @Override
        public String toString() {
            return left.toString() + " - " + right.toString() + " | " + triads;
        }
        
    }
    
}
