package br.com.geraldao.predicate;

import javax.persistence.criteria.Predicate.BooleanOperator;

/**
 * 
 * @author victor.bello
 *
 */
abstract class ConditionCombiner {

    private BooleanOperator operatorCombiner;

    BooleanOperator getOperatorCombiner() {
        return operatorCombiner;
    }

    void setOperatorCombiner(BooleanOperator operatorCombiner) {
        this.operatorCombiner = operatorCombiner;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[operatorCombiner=");
        builder.append(operatorCombiner);
        builder.append("]");
        return builder.toString();
    }

}
