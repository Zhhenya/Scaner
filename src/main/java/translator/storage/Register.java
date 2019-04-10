package translator.storage;

import translator.Storage;

public class Register extends Storage {
    public final int index;

    public Register(int i) {
        index = i;
    }

    @Override
    public String toString() {
        return REGISTERS[index];
    }

}