package br.com.geraldao.predicate;

import javax.persistence.Entity;
import javax.persistence.criteria.Predicate.BooleanOperator;

import br.com.geraldao.constant.Operator;
import br.com.geraldao.dao.BaseService;

/**
 * 
 * Class responsible to build <code>PredicateClause</code> used on <code> BaseService </code> to create JPA predicates query
 * 
 * @see PredicateClause
 * @see BaseService
 * @author victor.bello
 */
public class PredicateBuilder {

    private PredicateClause predicate;

    private PredicateBuilder(PredicateClause predicate) {
        this.predicate = predicate;
    }

    /**
     * Starts the where clause of a query using <code>{@link Operator#EQUAL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @param fieldValue
     *            - Condition value.
     * @return {@link PredicateBuilder}
     */
    public static PredicateBuilder where(String fieldName, Object fieldValue) {
        return where(fieldName, Operator.EQUAL, fieldValue);
    }

    /**
     * Starts the where clause of a query using <code>{@link Operator#ISNULL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @return {@link PredicateBuilder}
     */
    public static PredicateBuilder whereIsNull(String fieldName) {
        return where(fieldName, Operator.ISNULL, null);
    }

    /**
     * Starts the where clause of a query using <code>{@link Operator#NOT_NULL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @return {@link PredicateBuilder}
     */
    public static PredicateBuilder whereIsNotNull(String fieldName) {
        return where(fieldName, Operator.NOT_NULL, null);
    }

    /**
     * Starts the where clause of a query using selected <code>{@link Operator}</code>
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @param operator
     *            - Condition that should be applied of <code>{@link Operator}</code>
     * @param fieldValue
     *            - Condition value.
     * @return @return {@link PredicateBuilder}
     */
    public static PredicateBuilder where(String fieldName, Operator operator, Object fieldValue) {
        PredicateClause predicateClause = new PredicateClause();
        predicateClause.getQueries().add(new Condition(fieldName, operator, fieldValue));
        return new PredicateBuilder(predicateClause);
    }

    /**
     * Add a <b>conjunction</b> on current created where clause using <code>{@link Operator#NOT_NULL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @return @return {@link PredicateBuilder}
     */
    public PredicateBuilder andIsNotNull(String fieldName) {
        return and(fieldName, Operator.NOT_NULL, null);
    }

    /**
     * Add a <b>conjunction</b> on current created where clause using <code>{@link Operator#ISNULL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @return @return {@link PredicateBuilder}
     */
    public PredicateBuilder andIsNull(String fieldName) {
        return and(fieldName, Operator.ISNULL, null);
    }

    /**
     * Add a <b>conjunction</b> on current created where clause using <code>{@link Operator#EQUAL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @param fieldValue
     *            - Condition value.
     * @return {@link PredicateBuilder}
     */
    public PredicateBuilder and(String fieldName, Object fieldValue) {
        return and(fieldName, Operator.EQUAL, fieldValue);
    }

    /**
     * Add a <b>conjunction</b> on current created where clause using selected <code>{@link Operator}</code>
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @param operator
     *            - Condition that should be applied of <code>{@link Operator}</code>
     * @param fieldValue
     *            - Condition value.
     * @return @return {@link PredicateBuilder}
     */
    public PredicateBuilder and(String fieldName, Operator operator, Object fieldValue) {
        add(BooleanOperator.AND, fieldName, operator, fieldValue);
        return this;
    }

    /**
     * Add a <b>disjunction</b> on current created where clause using <code>{@link Operator#NOT_NULL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @return @return {@link PredicateBuilder}
     */
    public PredicateBuilder orIsNotNull(String fieldName) {
        return or(fieldName, Operator.NOT_NULL, null);
    }

    /**
     * Add a <b>disjunction</b> on current created where clause using <code>{@link Operator#ISNULL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @return @return {@link PredicateBuilder}
     */
    public PredicateBuilder orIsNull(String fieldName) {
        return or(fieldName, Operator.ISNULL, null);
    }

    /**
     * Add a <b>disjunction</b> on current created where clause using <code>{@link Operator#EQUAL}</code> as default
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @param fieldValue
     *            - Condition value.
     * @return {@link PredicateBuilder}
     */
    public PredicateBuilder or(String fieldName, Object fieldValue) {
        return or(fieldName, Operator.EQUAL, fieldValue);
    }

    /**
     * Add a <b>disjunction</b> on current created where clause using selected <code>{@link Operator}</code>
     * 
     * @param fieldName
     *            - Property name on class mapped by <code>{@link Entity}</code> annotation
     * @param operator
     *            - Condition that should be applied of <code>{@link Operator}</code>
     * @param fieldValue
     *            - Condition value.
     * @return @return {@link PredicateBuilder}
     */
    public PredicateBuilder or(String fieldName, Operator operator, Object fieldValue) {
        add(BooleanOperator.OR, fieldName, operator, fieldValue);
        return this;
    }

    /**
     * Add predicates using a <b>conjunction</b> as connector with the previously created predicates <br>
     * EG: SELECT * FROM Table WHERE field=1 <b>AND (field=3 and field=4 or field=5)</b>
     * 
     * @param predicateClause
     *            - Previously created and {@link PredicateBuilder#build()} predicates
     * @return {@link PredicateBuilder}
     */
    public PredicateBuilder andBlock(PredicateClause predicateClause) {
        checkPredicateState();
        predicateClause.setOperatorCombiner(BooleanOperator.AND);
        predicate.getQueries().add(predicateClause);
        return this;
    }

    /**
     * Add predicates using a <b>disjunction</b> as connector with the previously created predicates <br>
     * EG: SELECT * FROM Table WHERE field=1 <b>OR (field=3 and field=4 or field=5)</b>
     * 
     * @param predicateClause
     *            - Previously created and {@link PredicateBuilder#build()} predicates
     * @return {@link PredicateBuilder}
     */
    public PredicateBuilder orBlock(PredicateClause predicateClause) {
        checkPredicateState();
        predicateClause.setOperatorCombiner(BooleanOperator.OR);
        predicate.getQueries().add(predicateClause);
        return this;
    }

    /**
     * Build {@link PredicateClause} according with auxiliary methods
     * 
     * @return {@link PredicateClause}
     * @see PredicateBuilder
     */
    public PredicateClause build() {
        PredicateClause predicate = this.predicate;
        this.predicate = null;
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
            throw new IllegalStateException("Invalid state for variable predicate. If you already called build() method you should restart the process of generating a PredicateClause");
        }
    }

}
