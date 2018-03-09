package br.com.geraldao.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;

import br.com.geraldao.util.ListUtil;

import javax.persistence.criteria.Root;

/**
 * 
 * @author victor.bello
 *
 */
public class PredicateClause extends ConditionCombiner {
    private List<ConditionCombiner> queries = new ArrayList<ConditionCombiner>();

    /**
     * Generate JPA {@link Predicate} according to builded {@link PredicateClause}
     * 
     * @param root
     *            - Object entity mapped with {@link Entity} annotation
     * @param cb
     *            - CriteriaBuilder
     * @return List of JPA predicates
     * @see PredicateBuilder
     */
    public <T> List<Predicate> generator(Root<T> root, CriteriaBuilder cb) {
        return readPredicates(root, cb, queries);
    }

    private <T> List<Predicate> readPredicates(Root<T> root, CriteriaBuilder cb, List<ConditionCombiner> queries) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        List<Predicate> finalPredicates = new ArrayList<Predicate>();
        for (ConditionCombiner queryType : queries) {
            predicates.clear();
            if (queryType instanceof PredicateClause) {
                PredicateClause queryPredicate = (PredicateClause) queryType;
                predicates = readPredicates(root, cb, queryPredicate.queries);
            } else if (queryType instanceof Condition) {
                Condition query = (Condition) queryType;
                predicates.add(retrievePredicate(root, cb, query));
            }

            predicates.addAll(0, finalPredicates);
            finalPredicates.clear();
            if (queryType.getOperatorCombiner() != null && queryType.getOperatorCombiner() == BooleanOperator.OR) {
                finalPredicates.add(cb.or(predicates.toArray(new Predicate[predicates.size()])));
            } else {
                finalPredicates.add(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            }
        }
        return finalPredicates;
    }

    private <T> Predicate retrievePredicate(Root<T> root, CriteriaBuilder cb, Condition condition) {
        switch (condition.getOperator()) {
            case CONTAINS:
                return cb.like(root.<String> get(condition.getFieldName()), "%" + condition.getFieldValue() + "%");
            case ENDS_WITH:
                return cb.like(root.<String> get(condition.getFieldName()), "%" + condition.getFieldValue());
            case STARTS_WITH:
                return cb.like(root.<String> get(condition.getFieldName()), condition.getFieldValue() + "%");
            case IN:
                Collection<?> collection = (Collection<?>) condition.getFieldValue();
                if (ListUtil.isCollectionEmpty(collection)) {
                    // Se a listagem est� vazia, n�o se pode remover o In, pois retira o filtro, o que far� que apare�am itens que n�o deviam.
                    // Se mant�m o In, ocorre nullpointer. Por isso optei por colocar uma condi��o falsa.
                    Expression<Integer> param = cb.literal(1);
                    return cb.equal(param, -1);
                } else {
                    return cb.in(root.get(condition.getFieldName())).value(collection);
                }
            case NOT_IN:
                Collection<?> notInCollection = (Collection<?>) condition.getFieldValue();
                if (ListUtil.isCollectionEmpty(notInCollection)) {
                    // Se a listagem est� vazia, n�o se pode remover o In, pois retira o filtro, o que far� que apare�am itens que n�o deviam.
                    // Se mant�m o In, ocorre nullpointer. Por isso optei por colocar uma condi��o falsa.
                    Expression<Integer> param = cb.literal(1);
                    return cb.equal(param, -1);
                } else {
                    return cb.not(cb.in(root.get(condition.getFieldName())).value(notInCollection));
                }
            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo(root.<String> get(condition.getFieldName()), String.valueOf(condition.getFieldValue()));
            case NOT_EQUAL:
                return cb.notEqual(root.get(condition.getFieldName()), condition.getFieldValue());
            case EQUAL:
                return cb.equal(root.get(condition.getFieldName()), condition.getFieldValue());
            case NOT_NULL:
                return cb.isNotNull(root.get(condition.getFieldName()));
            case ISNULL:
                return cb.isNull(root.get(condition.getFieldName()));
            default:
                break;
        }
        return null;
    }

    List<ConditionCombiner> getQueries() {
        return queries;
    }

}
