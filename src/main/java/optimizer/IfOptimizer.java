package optimizer;

import triad.Reference;
import triad.Reference.*;
import triad.Triad;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class IfOptimizer {
    private List<Triad> original;
    private List<Triad> optimized;
    private List<Triad> originalCopy;

    public IfOptimizer(List<Triad> original) {
        this.original = new ArrayList<>(original);
        this.originalCopy = new ArrayList<>(original);
        optimized = new ArrayList<>();
    }

    public List<Triad> optimize() throws IOException {
        int i = 0, start_if = 0;
        while (i < original.size()) {
            Triad triad = original.get(i);
            if (triad.transfer != null && triad.transfer.equals(Triad.Transfer.START_IF)) {
                start_if = i;
            }
            if (triad.transfer != null && triad.transfer.equals(Triad.Transfer.IF)) {
                List<Triad> ifTriads = retrieveIf(original, i);
                List<Triad> elseTriads = retrieveElse(original, i);
                recalculateTriads(processIf(ifTriads, elseTriads), start_if);
                i += ifTriads.size() + elseTriads.size();
                continue;
            }
            i++;
        }

        PrintWriter out = new PrintWriter(new FileWriter("optimizationIf.txt"));
        for (int k = 0; k < originalCopy.size(); k++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(originalCopy.get(k).toString(k)).append("\t\t\t");
            if (k < original.size()) {
                stringBuilder.append(original.get(k).toString(k));
            }
            out.println(stringBuilder.toString());
        }
        out.close();
        return original;
    }

    private List<Triad> retrieveIf(List<Triad> origin, int start) {
        List<Triad> ifTriads = new ArrayList<>();
        for (int i = start; i < origin.size(); i++) {
            if (origin.get(i).transfer == Triad.Transfer.ELSE) {
                break;
            }
            ifTriads.add(origin.get(i));
        }
        return ifTriads;
    }

    private List<Triad> retrieveElse(List<Triad> origin, int start) {
        List<Triad> elseTriads = new ArrayList<>();
        for (int i = start; i < origin.size(); i++) {
            if (origin.get(i).transfer == Triad.Transfer.ELSE) {
                start = i;
                break;
            }
        }

        boolean isElseEnd = false;
        for (int i = start; i < origin.size(); i++) {
            if (origin.get(i).transfer == Triad.Transfer.END_ELSE) {
                isElseEnd = true;
                break;
            }
            elseTriads.add(origin.get(i));
        }
        if (!isElseEnd) {
            return null;
        }
        return elseTriads;
    }

    private OptimizedTriads processIf(List<Triad> ifTriad, List<Triad> elseTriad) {
        List<Triad> operators = new ArrayList<>();
        if (elseTriad.isEmpty()) {
            return null;
        }
        boolean isEquals = false;
        int i = 0;
        while (i < ifTriad.size()) {
            int j = 0;
            while (j < elseTriad.size()) {
                if (equalsTriads(ifTriad.get(i), elseTriad.get(j))) {
                    operators.add(ifTriad.get(i));
                    ifTriad.remove(i);
                    elseTriad.remove(j);
                    isEquals = true;
                } else {
                    j++;
                }
            }
            if (!isEquals) {
                i++;
            } else {
                isEquals = false;
            }
        }
        if (ifTriad.size() == 1) {
            ifTriad.clear();
        }
        if (elseTriad.size() == 1) {
            elseTriad.clear();
        }
        ifTriad.addAll(elseTriad);
        return new OptimizedTriads(operators, ifTriad);
    }

    private boolean equalsTriads(Triad triad, Triad triad1) {
        boolean isRef1Equals = false, isRef2Equals = false, actionEquals = false;
        if (triad.ref1 instanceof TriadReference && triad1.ref1 instanceof TriadReference) {
            if (((TriadReference) triad.ref1).index == ((TriadReference) triad1.ref1).index) {
                isRef1Equals = true;
            }
        }

        if (triad.ref2 instanceof TriadReference && triad1.ref2 instanceof TriadReference) {
            if (((TriadReference) triad.ref2).index == ((TriadReference) triad1.ref2).index) {
                isRef2Equals = true;
            }
        }

        if (triad.ref1 instanceof VariableReference && triad1.ref1 instanceof VariableReference) {
            if (((VariableReference) triad.ref1).variable.lexeme == ((VariableReference) triad1.ref1).variable.lexeme) {
                isRef1Equals = true;
            }
        }

        if (triad.ref2 instanceof VariableReference && triad1.ref2 instanceof VariableReference) {
            if (((VariableReference) triad.ref2).variable.lexeme == ((VariableReference) triad1.ref2).variable.lexeme) {
                isRef2Equals = true;
            }
        }

        if (triad.ref1 instanceof ConstantReference && triad1.ref1 instanceof ConstantReference) {
            if (((ConstantReference) triad.ref1).value == ((ConstantReference) triad1.ref1).value) {
                isRef1Equals = true;
            }
        }

        if (triad.ref2 instanceof ConstantReference && triad1.ref2 instanceof ConstantReference) {
            if (((ConstantReference) triad.ref2).value == ((ConstantReference) triad1.ref2).value) {
                isRef2Equals = true;
            }
        }

        if (triad.ref1 == null && triad1.ref1 == null) {
            isRef1Equals = true;
        }
        if (triad.ref2 == null && triad1.ref2 == null) {
            isRef2Equals = true;
        }
        if (triad.action == null && triad1.action == null || triad1.action.equals(triad.action)) {
            actionEquals = true;
        }
        return isRef1Equals && isRef2Equals && actionEquals;
    }

    private void recalculateTriads(OptimizedTriads optimizedTriads, int start) {
        if (optimizedTriads == null) {
            return;
        }
        LinkedList triads = new LinkedList(original);
        int i = 0;
        int operationSize = optimizedTriads.getOperators().size();
        int ifElseSize = optimizedTriads.getIfElseTriads().size();
        while (i < triads.size() - 1) {
            if (i + 1 == start) {
                int j = start, k = 0;
                while (k < operationSize) {
                    triads.add(j, optimizedTriads.getOperators().get(k));
                    k++; j++;
                }
                k = 0;
                while (k < ifElseSize) {
                    triads.add(j, optimizedTriads.getIfElseTriads().get(k));
                    k++; j++;
                }
                k = 0;
                while (k < operationSize + ifElseSize) {
                    triads.remove(j);
                    k++; j++;
                }
                break;
            }
            i++;
        }
        original = new ArrayList<>(triads);
    }

    static class OptimizedTriads {
        List<Triad> operators;
        List<Triad> ifElseTriads;

        OptimizedTriads(List<Triad> operators, List<Triad> ifElseTriads) {
            this.operators = operators;
            this.ifElseTriads = ifElseTriads;
        }

        List<Triad> getOperators() {
            return operators;
        }

        List<Triad> getIfElseTriads() {
            return ifElseTriads;
        }
    }

}