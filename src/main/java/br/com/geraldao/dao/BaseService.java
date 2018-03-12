package br.com.geraldao.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import br.com.geraldao.constant.QueryOrder;
import br.com.geraldao.entity.BaseEntity;
import br.com.geraldao.predicate.PredicateClause;

/**
 * Generic JPA Service
 * 
 * @author victor.bello
 *
 */
public abstract class BaseService extends QueryService {
    private final static Logger LOGGER = Logger.getLogger(BaseService.class);

    abstract protected EntityManager getEm();

    /**
     * Returns a JDBC Connection unwraping from entityManager the {@code Connection.class}. If vendor doesn't support this unwrap, a {@code RuntimeException} will be thrown. If connection acquired is null, a {@code NullPointerException} will be thrown.
     */
    @Override
    protected Connection connection() {
        String connectionUnwrapError = "Unable to get connection from entityManager. BaseService connection() method must be overrided and implemented according to JPA plataform vendor";
        try {
            Connection connection = getEm().unwrap(Connection.class);
            if (connection == null) {
                throw new NullPointerException(connectionUnwrapError);
            }
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(connectionUnwrapError + System.lineSeparator() + e.getMessage());
        }
    }

    public <T> boolean entityExists(Class<T> entity, PredicateClause predicateClause) {
        CriteriaBuilder cb = getEm().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entity);
        cq.select(cb.count(root));

        if (predicateClause != null) {
            List<Predicate> predicates = predicateClause.generator(root, cb);
            Predicate[] conditions = predicates.toArray(new Predicate[predicates.size()]);
            cq.where(conditions);
        }
        Long resultList = getEm().createQuery(cq).getSingleResult();
        return resultList > 0;
    }

    public <T extends BaseEntity> T save(T entity) {
        if (entity.getId() == null) {
            getEm().persist(entity);
            return entity;
        } else {
            return getEm().merge(entity);
        }
    }

    public <T extends BaseEntity> void saveBatch(Collection<T> items) {
        for (Iterator<T> iterator = items.iterator(); iterator.hasNext();) {
            T t = (T) iterator.next();
            if (t.getId() == null) {
                getEm().persist(t);
            } else {
                getEm().merge(t);
            }
        }
    }

    public <T extends BaseEntity> void remove(T entity) {
        getEm().remove(entity);
    }

    public <T extends BaseEntity> void remove(T entity, Object pk) {
        BaseEntity ref = getEm().getReference(entity.getClass(), pk);
        getEm().remove(ref);
    }

    /**
     * Remove todos os itens de acordo com o parametro passado
     * 
     * @param entityClass
     *            - Entidade na qual será realizada a pesquisa
     * @param params
     *            - Parametros de busca, ao realizar busca sem SpecificOperator, default é EQUAL
     * @return true se ao menos 1 item foi removido, false se nenhum item foi removido
     * @author victor.bello
     * @see {@link SpecificOperator} - {@literal Map<String,Object>}, sendo o segundo parametro SpecificOperator para utilização de condições especificas
     */
    public <T> Boolean removeByParams(Class<T> entityClass, PredicateClause predicateClause) {
        CriteriaBuilder cb = getEm().getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClass);
        Root<T> root = delete.from(entityClass);
        if (predicateClause != null) {
            List<Predicate> predicates = predicateClause.generator(root, cb);
            Predicate[] conditions = predicates.toArray(new Predicate[predicates.size()]);
            delete.where(conditions);
        }
        Query query = getEm().createQuery(delete);
        int rowCount = query.executeUpdate();
        if (rowCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * Atualiza todos os itens de acordo com o parametro passado
     * 
     * @param entityClass
     *            - Entidade na qual será realizada a pesquisa
     * @param values
     *            - Valores que serão atualizados
     * @param params
     *            - Parametros da condição "where", ao realizar busca sem SpecificOperator, default é EQUAL
     * @return true se ao menos 1 item foi atualizado, false se nenhum item foi atualizado
     * @author victor.bello
     * @see {@link SpecificOperator} - {@literal Map<String,Object>}, sendo o segundo parametro SpecificOperator para utilização de condições especificas
     */
    public <T> Boolean updateByParams(Class<T> entityClass, Map<String, Object> values, PredicateClause predicateClause) {
        CriteriaBuilder cb = getEm().getCriteriaBuilder();
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(entityClass);
        Root<T> root = update.from(entityClass);

        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("You must set values to update");
        }
        values.forEach((k, v) -> update.set(k, v));
        if (predicateClause != null) {
            List<Predicate> predicates = predicateClause.generator(root, cb);
            Predicate[] conditions = predicates.toArray(new Predicate[predicates.size()]);
            update.where(conditions);
        } else {
            LOGGER.debug("Be careful, you just entered on Isaac mode");
        }
        Query query = getEm().createQuery(update);
        int rowCount = query.executeUpdate();
        if (rowCount > 0) {
            return true;
        }
        return false;
    }

    // ***************************************************************************
    // ----------------------------- SEARCH QUERIES ----------------------------- //
    // ***************************************************************************
    public <T extends BaseEntity> T findById(Class<T> entityClass, Object id) {
        return getEm().find(entityClass, id);
    }

    public <T extends BaseEntity> T findFirstOrderedByParams(Class<T> entityClass, PredicateClause predicateClause, QueryOrder order, String... columns) {
        CriteriaQuery<T> cq = generateSelectQuery(entityClass, predicateClause, order, columns);
        List<T> resultList = getEm().createQuery(cq).setFirstResult(0).setMaxResults(1).getResultList();
        return resultList == null ? null : resultList.get(0);
    }

    public <T extends BaseEntity> T findFirstByParams(Class<T> entityClass, PredicateClause predicateClause) {
        return findFirstOrderedByParams(entityClass, predicateClause, null);
    }

    public <T extends BaseEntity> T findFirstOrdered(Class<T> entityClass, QueryOrder order, String... columns) {
        return findFirstOrderedByParams(entityClass, null, order, columns);
    }

    public <T extends BaseEntity> List<T> findAll(Class<T> entityClass) {
        return findAllOrderedByParams(entityClass, null, null);
    }

    public <T extends BaseEntity> List<T> findAllOrderedByParams(Class<T> entityClass, PredicateClause predicateClause, QueryOrder order, String... columns) {
        CriteriaQuery<T> cq = generateSelectQuery(entityClass, predicateClause, order, columns);
        List<T> resultList = getEm().createQuery(cq).getResultList();
        return resultList == null ? Collections.emptyList() : resultList;
    }

    public <T extends BaseEntity> List<T> findAllByParams(Class<T> entityClass, PredicateClause predicateClause) {
        return findAllOrderedByParams(entityClass, predicateClause, null);
    }

    public <T extends BaseEntity> List<T> findAllOrdered(Class<T> entityClass, QueryOrder order, String... columns) {
        return findAllOrderedByParams(entityClass, null, order, columns);
    }

    public boolean isLoaded(BaseEntity entity) {
        PersistenceUnitUtil unitUtil = getEm().getEntityManagerFactory().getPersistenceUnitUtil();
        return unitUtil.isLoaded(entity);
    }

    private <T extends BaseEntity> CriteriaQuery<T> generateSelectQuery(Class<T> entityClass, PredicateClause predicateClause, QueryOrder order, String... columns) {
        CriteriaBuilder cb = getEm().getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        if (predicateClause != null) {
            List<Predicate> predicates = predicateClause.generator(root, cb);
            Predicate[] conditions = predicates.toArray(new Predicate[predicates.size()]);
            cq.where(conditions);
        }

        if (order != null) {
            if (columns == null) {
                throw new IllegalArgumentException("You have chosen to order, but haven't passed which columns to order by");
            }
            List<Order> orders = new ArrayList<>();
            for (String column : columns) {
                if (order == QueryOrder.ASC) {
                    orders.add(cb.asc(root.get(column)));
                } else {
                    orders.add(cb.desc(root.get(column)));
                }
            }
            cq.orderBy(orders);
        }
        return cq;
    }

    // ***************************************************************************
    // ----------------------------- NATIVE QUERIES ----------------------------- //
    // ***************************************************************************
    protected <T extends BaseEntity> List<T> findByQuery(Class<T> entityClass, String query, Object... params) {
        TypedQuery<T> tquery = getEm().createQuery(query, entityClass);
        int idx = 1;
        for (Object param : params) {
            tquery.setParameter(idx, param);
            idx++;
        }
        return tquery.getResultList();
    }

    @SuppressWarnings("unchecked")
    protected <T extends BaseEntity> List<T> findByNativeQuery(String query, Class<T> clazz, Object... params) {
        Query tquery = null;
        if (clazz == null) {
            tquery = getEm().createNativeQuery(query, clazz);
        } else {
            tquery = getEm().createNativeQuery(query, clazz);
        }
        int idx = 1;
        for (Object param : params) {
            tquery.setParameter(idx, param);
            idx++;
        }
        return tquery.getResultList();
    }

    protected <T extends BaseEntity> List<T> findByNativeQuery(String query, Object... params) {
        return findByNativeQuery(query, null, params);
    }

    protected void removeByNativeQuery(String query, Object... params) {
        Query tquery = getEm().createNativeQuery(query);
        int idx = 1;
        for (Object param : params) {
            tquery.setParameter(idx, param);
            idx++;
        }
        tquery.executeUpdate();
    }

    protected void updateByNativeQuery(String query, Object... params) {
        Query tquery = getEm().createNativeQuery(query);
        int idx = 1;
        for (Object param : params) {
            tquery.setParameter(idx, param);
            idx++;
        }
        tquery.executeUpdate();
    }

    protected Query getCreatedNativeQuery(String sql) {
        return getEm().createNativeQuery(sql);
    }

}
