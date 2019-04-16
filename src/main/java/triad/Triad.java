package triad;

public class Triad {

    public enum Action {
        proc, clazz, push, call, endp, nop, cmp, jg, jmp,
        assign("="), ret("return"),
        eq("=="), ne("!="), lt("<"), le("<="), gt(">"), ge(">="),
        add("+"), sub("-"), mul("*"), div("/"), mod("%"), neg,
        skip;

        private final String representation;

        Action() {
            this(null);
        }

        Action(String s) {
            representation = s;
        }

        @Override
        public String toString() {
            if (representation == null) {
                return super.toString();
            }
            return representation;
        }
    }

    public enum Transfer {FUNCTION, IF, ELSE, END_ELSE}

    public Action action;
    public Reference ref1 = null, ref2 = null;

    public Transfer transfer = null;

    public Triad(Action a) {
        action = a;
    }

    public Triad(Action a, Reference b) {
        action = a; ref1 = b;
    }

    public Triad(Action a, Reference b, Reference c) {
        action = a; ref1 = b; ref2 = c;
    }

    public String toString(int number) {
        if (action == Action.skip) {
            return "     ----";
        }
        return String.format("%3d) %s", number, toString());
    }

    public Triad setTransferControl(Transfer t) {
        transfer = t;
        return this;
    }

    @Override
    public String toString() {
        String s1 = ref1 == null ? "" : ref1.toString();
        String s2 = ref2 == null ? "" : ref2.toString();
        return String.format("%-10s %-8s %-8s", action, s1, s2);
    }

}
