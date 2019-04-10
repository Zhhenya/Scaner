package translator;

import tree.Tree;

public abstract class Storage {

    public static final String[] REGISTERS = new String[]{"eax", "ebx", "ecx", "edx", "esi", "edi"};

    public static class Register extends Storage {

        public final int index;

        public Register(int i) {
            index = i;
        }

        @Override
        public String toString() {
            return REGISTERS[index];
        }

    }

    public static class Memory extends Storage {

        public final String address;
        public int index = -1;

        private Memory(boolean global, String globalName, int addr) {
            if (global) {
                address = globalName;
            } else {
                String offset = "";

                if (addr > 0) {
                    offset = "+" + addr;
                } else if (addr < 0) {
                    offset = addr + "";
                }

                address = "ebp" + offset;
            }
        }

        public Memory(Tree n) {
            this(n.global, n.value, n.address);
        }

        public Memory(int address, int index) {
            this(false, null, address);
            this.index = index;
        }

        @Override
        public String toString() {
            return "dword ptr [" + address + "]";
        }

    }

    public static class Immediate extends Storage {

        public final long value;

        public Immediate(long val) {
            value = val;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }

    }

}
