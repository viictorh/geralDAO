package br.com.geraldao.predicate;

import br.com.geraldao.constant.Operator;

/**
 * 
 * @author victor.bello
 *
 */
class Condition extends ConditionCombiner {
    private Operator operator;
    private String   fieldName;
    private Object   fieldValue;

    public Condition(String fieldName, Operator operator, Object fieldValue) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.fieldValue = fieldValue;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        return "Condition [operator=" + operator + ", fieldName=" + fieldName + ", fieldValue=" + fieldValue + super.toString() + "]";
    }

}
