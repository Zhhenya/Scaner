package optimizer;

import triad.Triad;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class IfOptimizer {
    private List<Triad> original;
    private List<Triad> optimized;

    public IfOptimizer(List<Triad> original) {
        this.original = new ArrayList<>(original);
        optimized = new ArrayList<>();
    }

    public List<Triad> optimize() throws IOException {
        int i = 0;
        while (i < original.size()) {
            Triad triad = original.get(i);
            if (triad.transfer.equals(Triad.Transfer.IF)) {
                List<Triad> ifTriads = retrieveIf(original, i);
                List<Triad> elseTriads = retrieveElse(original, i);
                recalculateTriads(processIf(ifTriads, elseTriads), i);
                i += ifTriads.size() + elseTriads.size();
                continue;
            }
            optimized.add(triad);
            i++;
        }

        PrintWriter out = new PrintWriter(new FileWriter("optimizationIf.txt"));
        for (int k = 0; k < original.size(); k++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(original.get(k).toString(k)).append("\t\t\t");
            if (k < optimized.size()) {
                stringBuilder.append(optimized.get(k).toString(k));
            }
            out.println(stringBuilder.toString());
        }
        out.close();
        return optimized;
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

        for (int i = start; i < origin.size(); i++) {
            if (origin.get(i).transfer == Triad.Transfer.END_ELSE) {
                break;
            }
            elseTriads.add(origin.get(i));
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
                if (isEquals = equal(ifTriad.get(i), elseTriad.get(j))) {
                    operators.add(ifTriad.get(i));
                    ifTriad.remove(i);
                    elseTriad.remove(j);
                } else {
                    j++;
                }
            }
            if (!isEquals) {
                i++;
            }
        }
        ifTriad.addAll(elseTriad);
        return new OptimizedTriads(operators, ifTriad);
    }

    private boolean equal(Triad triad, Triad triad1) {
        return triad.action.equals(triad1.action) && triad.ref2.equals(triad1.ref2) && triad.ref1.equals(triad1.ref1);
    }

    private void recalculateTriads(OptimizedTriads optimizedTriads, int start) {
        if (optimizedTriads == null) {
            return;
        }
        LinkedList triads = new LinkedList(original);
        int i = 0;
        while (i < triads.size() - 1) {
            if (i + 1 == start) {
                int j = start, k = 0;
                while (k < optimizedTriads.getOperators().size()) {
                    triads.add(j, optimizedTriads.getOperators().get(k));
                    k++; j++;
                }
                k = 0;
                while (k < optimizedTriads.getIfElseTriads().size()) {
                    triads.add(j, optimizedTriads.getIfElseTriads().get(k));
                    k++; j++;
                }
                break;
            }
        }
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