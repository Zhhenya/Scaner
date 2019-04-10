package tree;

import service.DataType;

public class DataValue {
    public DataType type = DataType.Empty;
    public Value value = new Value();

    public DataValue clone() {
        DataValue dataValue = new DataValue();
        dataValue.type = this.type;
        dataValue.value.valueInt = this.value.valueInt;
        dataValue.value.constant = this.value.constant;
        dataValue.value.clazz = this.value.clazz;
        return dataValue;
    }
}