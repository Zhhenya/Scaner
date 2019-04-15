package tree;

import service.DataType;

public class Value {

    public DataType type;
    public Comparable value;

    public Value(DataType type, Comparable value) {
        this.type = type;
        this.value = value;
    }

    public Value() {
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getValue() {
        return value.toString();
    }

    public void setValue(Comparable value) {
        this.value = value;
    }
}
