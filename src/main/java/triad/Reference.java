package triad;

import tree.Tree;

public abstract class Reference {
    
    public static class VariableReference extends Reference {
        
        public final Tree variable;
        
        public VariableReference(Tree node) {
            variable = node;
        }
        
        @Override
        public String toString() {
            //if(variable.global) return "{" + variable.lexeme.value + "}";
            return variable.node.dataValue.value.valueInt.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof VariableReference && variable == ((VariableReference)o).variable;
        }
        
        @Override
        public int hashCode() {
            return variable.hashCode();
        }
        
    }
    
    public static class ConstantReference extends Reference {
        
        public final long value;
        
        public ConstantReference(long val) {
            value = val;
        }
        
        @Override
        public String toString() {
            return "" + value;
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof ConstantReference && value == ((ConstantReference)o).value;
        }
        
        @Override
        public int hashCode() {
            return (int)value;
        }
        
    }
    
    public static class TriadReference extends Reference {
        
        public final int index;
        
        public TriadReference(int i) {
            index = i;
        }
        
        @Override
        public String toString() {
            return "[" + index + "]";
        }
        
    }
    
    public static class FunctionReference extends Reference {

        public final String name;

        public FunctionReference(String n) {
            name = n;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
    }
    
}
