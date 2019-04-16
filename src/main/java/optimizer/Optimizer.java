package optimizer;

import triad.Reference;
import triad.Reference.*;
import triad.Triad;
import triad.Triad.Action;
import triad.Triad.Transfer;
import optimizer.Aggregate.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Optimizer {

    private final Triad[] triads, original;
    private final Aggregate[] aggregates;
    private final boolean[] used;

    public Optimizer(List<Triad> input) {
        original = input.toArray(new Triad[0]);
        triads = input.toArray(new Triad[0]);

        aggregates = new Aggregate[triads.length];
        used = new boolean[triads.length];
    }

    public Triad[] optimize(boolean silent) throws Exception {
        boolean function = false;
        for (int i = 0; i < aggregates.length; i++) {
            Triad current = triads[i];
            aggregates[i] = buildAggregate(i);

            if (current.action == Action.proc) {
                function = true;
            }
            if (current.action == Action.endp) {
                function = false;
            }

            if (function && aggregates[i] == null) {
                if (current.ref1 instanceof TriadReference) {
                    tryOptimize(triads[i], ((TriadReference) current.ref1).index);
                }
                if (current.ref2 instanceof TriadReference) {
                    tryOptimize(triads[i], ((TriadReference) current.ref2).index);
                }
            }
        }

        List<Triad> optimized = clean();

        PrintWriter out = new PrintWriter(new FileWriter("optimizationExpression.txt"));
        for (int k = 0; k < triads.length; k++) {
            String x = original[k].toString(k);
            if (k < optimized.size()) {
                out.println(x + "\t\t\t" + optimized.get(k).toString(k));
            } else {
                out.println(x);
            }
        }
        out.close();

        if (!silent) {
            System.out.println();
        }
        return triads;
    }

    private List<Triad> clean() {
        List<Triad> optimized = Arrays.stream(triads)
                                      .filter(triad -> !triad.action.equals(Action.skip))
                                      .collect(Collectors.toList());
        List<Triad> optimizedCopy = new ArrayList<>(optimized);
        for (int i = 0; i < optimized.size(); i++) {
            Triad optimizedTriad = optimized.get(i);
            Triad newTriad = new Triad(optimizedTriad.action, optimizedTriad.ref1, optimizedTriad.ref2);
            Reference ref1 = optimizedTriad.ref1;
            if (ref1 instanceof TriadReference) {
                newTriad.ref1 = new TriadReference(optimizedCopy.indexOf(triads[((TriadReference) ref1).index]));
            }
            Reference ref2 = optimizedTriad.ref2;
            if (ref2 instanceof TriadReference) {
                newTriad.ref2 = new TriadReference(optimizedCopy.indexOf(triads[((TriadReference) ref2).index]));
            }
            optimized.set(i, newTriad);
        }
        return optimized;
    }

    private Aggregate buildAggregate(int i) {
        Triad current = triads[i];
        Aggregate agg = null;

        if (current.action == Action.mul || current.action == Action.div) {
            agg = new MultiplicationAggregate();
        }
        if (current.action == Action.add || current.action == Action.sub) {
            agg = new AdditionAggregate();
        }

        if (agg == null) {
            return null;
        }
        Reference a = current.ref1, b = current.ref2;

        boolean flag;
        if (current.action == Action.mul || current.action == Action.add) {
            flag = mergeAggregates(a, agg, agg.left, agg.right) && mergeAggregates(b, agg, agg.left, agg.right);
        } else {
            flag = mergeAggregates(a, agg, agg.left, agg.right) && mergeAggregates(b, agg, agg.right, agg.left);
        }
        if (!flag) {
            return null;
        }

        agg.triads.add(i);
        Collections.sort(agg.triads);

        return agg;
    }

    private boolean mergeAggregates(Reference ref, Aggregate agg, List<Reference> left, List<Reference> right) {
        if (ref instanceof TriadReference) {
            Aggregate other = aggregates[((TriadReference) ref).index];
            if (other == null || other.getClass() != agg.getClass()) {
                return false;
            }

            left.addAll(other.left);
            right.addAll(other.right);

            agg.triads.addAll(other.triads);
        } else {
            left.add(ref);
        }
        return true;
    }

    private void tryOptimize(Triad triad, int i) {
        if (aggregates[i] == null) {
            return;
        }

        Aggregate agg = aggregates[i], clone = agg.copy();
        int from = clone.triads.get(0) - 1;

        int loops = 0;
        for (int j = from; j >= 0; j--) {
            /* Do actions only with first valid intersection */
            if (used[i]) {
                break;
            }

            /* Stop search if function bounds reached */
            if (triads[j].transfer == Transfer.FUNCTION) {
                break;
            }

            /* Remove variable if assigned */
            if (triads[j].action == Action.assign) {
                clone.remove(triads[j].ref1);
            }

            /* Remove global variables if function call */
            if (triads[j].action == Action.call) {
                clone.removeGlobalVariables();
            }

            /* Stop search if possible intersection size one or zero */
            if (clone.size() <= 1) {
                break;
            }

            /* Check for loops */
          /*  if (triads[j].transfer == Transfer.ELSE) {
                loops++;
            }
            if (triads[j].transfer == Transfer.IF) {
                loops--;
            }
            if (loops != 0) {
                continue;
            }*/

            /* Intersection check code */
            Aggregate check = aggregates[j];
            if (check == null || check.getClass() != agg.getClass()) {
                continue;
            }

            List<Reference> iLeft = intersection(clone.left, check.left);
            List<Reference> iRight = intersection(clone.right, check.right);
            int size1 = iLeft.size() + iRight.size();

            List<Reference> iLeftRev = intersection(clone.left, check.right);
            List<Reference> iRightRev = intersection(clone.right, check.left);
            int size2 = iLeftRev.size() + iRightRev.size();

            if (size1 > 1 && size1 > size2) {
                rebuild(triad, i, j, iLeft, iRight, false);
            } else if (size2 > 1) {
                rebuild(triad, i, j, iLeftRev, iRightRev, true);
            }
        }
    }

    private <E> List<E> intersection(List<? extends E> list1, List<? extends E> list2) {
        ArrayList<E> tmp = new ArrayList<>(list1), result = new ArrayList<>();
        for (E element : list2) {
            if (tmp.contains(element)) {
                tmp.remove(element);
                result.add(element);
            }
        }
        return result;
    }

    private <E> List<E> exclude(List<? extends E> from, List<? extends E> elements) {
        ArrayList<E> result = new ArrayList<>(from);
        for (E element : elements) {
            if (result.contains(element)) {
                result.remove(element);
            }
        }
        return result;
    }

    private void rebuild(Triad triad, int i, int j, List<Reference> left, List<Reference> right, boolean reverse) {
        Aggregate current = aggregates[i], previous = aggregates[j];
        boolean multiplication = current instanceof MultiplicationAggregate;

        /* Skip already splitted aggregates, which not fully used */
        int size = left.size() + right.size();
        if (size < previous.size() && used[j]) {
            return;
        }

        /* Split previous aggregate */
        Pair<Reference, Boolean> common = Pair.of(new TriadReference(j), false);
        if (size < previous.size()) {
            List<Reference> l = !reverse ? left : right, r = !reverse ? right : left;

            common = buildTriads(l, r, previous.triads.subList(0, size - 1), multiplication);
            buildRemainder(previous, l, r, size, multiplication, j, common);
        }

        common = Pair.of(common.getKey(), common.getValue() ^ reverse);

        /* Split current aggregate */
        for (int k = 0; k < size - 1; k++) {
            triads[current.triads.get(k)] = new Triad(Action.skip);
        }

        Pair<Reference, Boolean> remainder = null;
        if (size == current.size()) {
            if (triad.ref1 instanceof TriadReference && ((TriadReference) triad.ref1).index == i) {
                triad.ref1 = common.getKey();
            }
            if (triad.ref2 instanceof TriadReference && ((TriadReference) triad.ref2).index == i) {
                triad.ref2 = common.getKey();
            }
            used[i] = true;
        } else {
            remainder = buildRemainder(current, left, right, size, multiplication, i, common);
        }

        for (int index : previous.triads) {
            aggregates[index] = buildAggregate(index);
        }
        for (int index : current.triads) {
            aggregates[index] = buildAggregate(index);
        }

        if (remainder != null && remainder.getKey() instanceof TriadReference) {
            tryOptimize(triads[i], ((TriadReference) remainder.getKey()).index);
        }
    }

    private Pair<Reference, Boolean> buildTriads(List<Reference> left, List<Reference> right, List<Integer> indices,
                                                 boolean multiplication) {
        if (left.size() == 1 && right.size() == 0) {
            return Pair.of(left.get(0), false);
        }
        if (left.size() == 0 && right.size() == 1) {
            return Pair.of(right.get(0), true);
        }

        ArrayList<Pair<Reference, Action>> actions = new ArrayList<>(indices.size() + 1);
        Action a = multiplication ? Action.mul : Action.add, b = multiplication ? Action.div : Action.sub;

        boolean reverse = false;
        if (left.size() != 0) {
            for (Reference ref : left) {
                actions.add(Pair.of(ref, a));
            }
            for (Reference ref : right) {
                actions.add(Pair.of(ref, b));
            }
        } else {
            reverse = true;
            for (Reference ref : right) {
                actions.add(Pair.of(ref, a));
            }
        }

        int actionsPtr = 0;
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);

            if (actionsPtr == 0) {
                Pair<Reference, Action> first = actions.get(0), second = actions.get(1);
                triads[index] = new Triad(second.getRight(), first.getKey(), second.getKey());

                actionsPtr++;
            } else {
                Pair<Reference, Action> first = actions.get(actionsPtr);
                triads[index] = new Triad(first.getRight(), new TriadReference(indices.get(i - 1)), first.getKey());
            }

            actionsPtr++;
        }

        return Pair.of(new TriadReference(indices.get(indices.size() - 1)), reverse);
    }

    private Pair<Reference, Boolean> buildRemainder(Aggregate aggregate, List<Reference> left, List<Reference> right,
                                                    int size, boolean multiplication, int index,
                                                    Pair<Reference, Boolean> common) {
        List<Reference> exLeft = exclude(aggregate.left, left), exRight = exclude(aggregate.right, right);

        List<Integer> sub;
        if (!common.getValue() || exLeft.size() != 0) {
            sub = aggregate.triads.subList(size - 1, aggregate.triads.size() - 1);
        } else {
            /* both parts reversed, two triads required */
            sub = aggregate.triads.subList(size - 2, aggregate.triads.size() - 2);
        }

        Action main = aggregate instanceof MultiplicationAggregate ? Action.mul : Action.add;
        Action rev = aggregate instanceof MultiplicationAggregate ? Action.div : Action.sub;

        Pair<Reference, Boolean> remainder = buildTriads(exLeft, exRight, sub, multiplication);

        if (!common.getValue() && !remainder.getValue()) {
            triads[index] = new Triad(main, common.getKey(), remainder.getKey());
        } else if (common.getValue() && remainder.getValue()) {
            int prevIndex = aggregate.triads.get(aggregate.triads.size() - 2);
            int constVal = aggregate instanceof MultiplicationAggregate ? 1 : 0;

            triads[prevIndex] = new Triad(main, common.getKey(), remainder.getKey());
            triads[index] = new Triad(rev, new ConstantReference(constVal), new TriadReference(prevIndex));
        } else if (remainder.getValue()) {
            triads[index] = new Triad(rev, common.getKey(), remainder.getKey());
        } else {
            triads[index] = new Triad(rev, remainder.getKey(), common.getKey());
        }

        used[index] = true;
        return remainder;
    }

}
