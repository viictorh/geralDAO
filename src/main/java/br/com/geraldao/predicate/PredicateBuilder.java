package br.com.geraldao.predicate;

import javax.persistence.criteria.Predicate.BooleanOperator;

import br.com.geraldao.constant.Operator;

public class PredicateBuilder {

    private PredicateClause predicate;

    private PredicateBuilder(PredicateClause predicate) {
        this.predicate = predicate;
    }

    public static PredicateBuilder where(String fieldName, Object fieldValue) {
        return where(fieldName, Operator.EQUAL, fieldValue);
    }

    public static PredicateBuilder where(String fieldName, Operator operator, Object fieldValue) {
        PredicateClause qp = new PredicateClause();
        qp.getQueries().add(new Condition(fieldName, operator, fieldValue));
        return new PredicateBuilder(qp);
    }

    public PredicateBuilder andIsNotNull(String fieldName) {
        return and(fieldName, Operator.NOT_NULL, null);
    }

    public PredicateBuilder andIsNull(String fieldName) {
        return and(fieldName, Operator.ISNULL, null);
    }

    public PredicateBuilder and(String fieldName, Object fieldValue) {
        return and(fieldName, Operator.EQUAL, fieldValue);
    }

    public PredicateBuilder and(String fieldName, Operator operator, Object fieldValue) {
        add(BooleanOperator.AND, fieldName, operator, fieldValue);
        return this;
    }

    public PredicateBuilder orIsNotNull(String fieldName) {
        return or(fieldName, Operator.NOT_NULL, null);
    }

    public PredicateBuilder orIsNull(String fieldName) {
        return or(fieldName, Operator.ISNULL, null);
    }

    public PredicateBuilder or(String fieldName, Object fieldValue) {
        return or(fieldName, Operator.EQUAL, fieldValue);
    }

    public PredicateBuilder or(String fieldName, Operator operator, Object fieldValue) {
        add(BooleanOperator.OR, fieldName, operator, fieldValue);
        return this;
    }

    public PredicateBuilder andBlock(PredicateClause q) {
        checkPredicateState();
        q.setOperatorCombiner(BooleanOperator.AND);
        predicate.getQueries().add(q);
        return this;
    }

    public PredicateBuilder orBlock(PredicateClause q) {
        checkPredicateState();
        q.setOperatorCombiner(BooleanOperator.OR);
        predicate.getQueries().add(q);
        return this;
    }

    public PredicateClause build() {
        return predicate;
    }

    private void add(BooleanOperator combiner, String fieldName, Operator operator, Object fieldValue) {
        checkPredicateState();
        Condition query = new Condition(fieldName, operator, fieldValue);
        query.setOperatorCombiner(combiner);
        predicate.getQueries().add(query);
    }

    private void checkPredicateState() {
        if (predicate == null) {
            throw new IllegalStateException("Invalid state for variable predicate. Probably wrong usage of PredicateBuilder.");
        }
    }

}
