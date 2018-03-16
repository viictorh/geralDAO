package br.com.geraldao.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import br.com.geraldao.predicate.PredicateBuilder;
import br.com.geraldao.predicate.PredicateClause;

/**
 * Generic JPA Service
 * 
 * @author victor.bello
 *
 */
public abstract class BaseService extends QueryService {
    private final static Logger LOGGER = Logger.getLogger(BaseService.class);

    /**
     * @see EntityManager
     * @return
     * @author victor.bello
     */
    abstract protected EntityManager getEm();

    /**
     * Returns a JDBC Connection unwraping from entityManager the {@code Connection.class}. If vendor doesn't support this unwrap, a {@code RuntimeException} will be thrown. If connection acquired is null, a {@code NullPointerException} will be thrown.
     */
    @Override
    protected Connection connection() {
        String connectionUnwrapError = "Unable to get connection from entityManager. BaseService connection() method must be overrided and implemented according to JPA plataform vendor";
        try {
            EntityManager em = getEm();
            Connection connection = em.unwrap(Connection.class);
            if (connection == null) {
                throw new NullPointerException(connectionUnwrapError);
            }
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(connectionUnwrapError + System.lineSeparator() + e.getMessage());
        }
    }

    /**
     * Searchs for an entity class according to predicateClause.
     * 
     * @param entity
     *            - desired mapped entity
     * @param predicateClause
     *            - desired conditions to filter
     * @return - <b>true</b> if there is an entity according parameters, <b>false</b> otherwise.
     * @see PredicateClause
     * @see PredicateBuilder
     */
    public <T> boolean entityExists(Class<T> entity, PredicateClause predicateClause) {
        EntityManager em = getEm();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entity);
        cq.select(cb.count(root));

        if (predicateClause != null) {
            List<Predicate> predicates = predicateClause.generator(root, cb);
            Predicate[] conditions = predicates.toArray(new Predicate[predicates.size()]);
            cq.where(conditions);
        }
        Long resultList = em.createQuery(cq).getSingleResult();
        return resultList > 0;
    }

    /**
     * Method responsible for saving (creating new object) or updating on database.
     * 
     * @param entity
     *            - Desired mapped entity object to save
     * @return - Optional with updated object with new ID (if new object)
     * @throws NullPointerException
     *             if optional is null
     */
    public <T extends BaseEntity> Optional<T> save(T entity) {
        EntityManager em = getEm();
        if (entity.getId() == null) {
            em.persist(entity);
            return Optional.of(entity);
        } else {
            return Optional.of(em.merge(entity));
        }
    }

    /**
     * Method responsible for saving (creating new objects) or updating on database.
     * 
     * @param items
     *            - Collection with items to save
     */
    public <T extends BaseEntity> void saveBatch(Collection<T> items) {
        EntityManager em = getEm();
        for (Iterator<T> iterator = items.iterator(); iterator.hasNext();) {
            T t = (T) iterator.next();
            if (t.getId() == null) {
                em.persist(t);
            } else {
                em.merge(t);
            }
        }
    }

    /**
     * Method responsible to remove (delete from) item from table.
     * 
     * @param entity
     *            - Item desired to remove
     */
    public <T extends BaseEntity> void remove(T entity) {
        EntityManager em = getEm();
        BaseEntity ref = em.getReference(entity.getClass(), entity.getId());
        em.remove(ref);
    }

    /**
     * Method responsible to remove all items according to desired arguments.
     * 
     * @param entityClass
     *            - Entity (table) which item(s) should be removed.
     * @param predicateClause
     *            - Where condition to select and remove items
     * @return <b>true</b> if at least one item has been removed. <b>false</b> otherwise
     * @author victor.bello
     * @see PredicateClause
     * @see PredicateBuilder
     */
    public <T> boolean removeByParams(Class<T> entityClass, PredicateClause predicateClause) {
        EntityManager em = getEm();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClass);
        Root<T> root = delete.from(entityClass);
        if (predicateClause != null) {
            List<Predicate> predicates = predicateClause.generator(root, cb);
            Predicate[] conditions = predicates.toArray(new Predicate[predicates.size()]);
            delete.where(conditions);
        }
        Query query = em.createQuery(delete);
        int rowCount = query.executeUpdate();
        if (rowCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * Method responsible to update all items according to desired arguments.
     * 
     * @param entityClass
     *            - Entity (table) which item(s) should be updated.
     * @param values
     *            - Map which defines SET option of the update statement. Key is the column name and Value is the new value to be updated
     * @param predicateClause
     *            - Where condition to select and update items
     * @return <b>true</b> if at least one item has been updated. <b>false</b> otherwise.
     * @author victor.bello
     * @see PredicateClause
     * @see PredicateBuilder
     */

    public <T> boolean updateByParams(Class<T> entityClass, Map<String, Object> values, PredicateClause predicateClause) {
        EntityManager em = getEm();
        CriteriaBuilder cb = em.getCriteriaBuilder();
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
        Query query = em.createQuery(update);
        int rowCount = query.executeUpdate();
        if (rowCount > 0) {
            return true;
        }
        return false;
    }

    // ***************************************************************************
    // ----------------------------- SEARCH QUERIES ----------------------------- //
    // ***************************************************************************
    /**
     * Search on table by id value
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @param id
     *            - value condition to seek
     * @return - Object Encapsulated on {@link Optional} interface based on entityClass type
     * @author victor.bello
     * @see Optional
     */
    public <T extends BaseEntity> Optional<T> findById(Class<T> entityClass, Object id) {
        return Optional.ofNullable(getEm().find(entityClass, id));
    }

    /**
     * Method responsible to retrieve the <b>first</b> result on a table with its result ordered and filtered
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @param predicateClause
     *            - Where condition to filter
     * @param order
     *            - Return ordem of the results. {@link QueryOrder#ASC} will return the result in ascending order. The {@link QueryOrder#DESC} will return the result in descending order.
     * @param columns
     *            - When {@link QueryOrder} is passed by argument, this argument becomes required. This parameter informs the columns which should ordered by
     * @return - Returns the <b>FIRST</b> item found encapsulated on {@link Optional} interface based on entityClass type
     * @author victor.bello
     * @see PredicateClause
     * @see PredicateBuilder
     */
    public <T extends BaseEntity> Optional<T> findFirstOrderedByParams(Class<T> entityClass, PredicateClause predicateClause, QueryOrder order, String... columns) {
        EntityManager em = getEm();
        CriteriaQuery<T> cq = generateSelectQuery(em, entityClass, predicateClause, order, columns);
        List<T> resultList = em.createQuery(cq).setFirstResult(0).setMaxResults(1).getResultList();
        return resultList == null ? Optional.empty() : Optional.ofNullable(resultList.get(0));
    }

    /**
     * Method responsible to retrieve the <b>first</b> result on a table with its result filtered
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @param predicateClause
     *            - Where condition to filter
     * @return - Returns the <b>FIRST</b> item found encapsulated on {@link Optional} interface based on entityClass type
     * @author victor.bello
     * @see PredicateClause
     * @see PredicateBuilder
     */
    public <T extends BaseEntity> Optional<T> findFirstByParams(Class<T> entityClass, PredicateClause predicateClause) {
        return findFirstOrderedByParams(entityClass, predicateClause, null);
    }

    /**
     * Method responsible to retrieve the <b>first</b> result on a table with its result ordered
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @param order
     *            - Return ordem of the results. {@link QueryOrder#ASC} will return the result in ascending order. The {@link QueryOrder#DESC} will return the result in descending order.
     * @param columns
     *            - When {@link QueryOrder} is passed by argument, this argument becomes required. This parameter informs the columns which should ordered by
     * @return - Returns the <b>FIRST</b> item found encapsulated on {@link Optional} interface based on entityClass type
     * @author victor.bello
     * 
     */
    public <T extends BaseEntity> Optional<T> findFirstOrdered(Class<T> entityClass, QueryOrder order, String... columns) {
        return findFirstOrderedByParams(entityClass, null, order, columns);
    }

    /**
     * Method responsible to retrieve the <b>first</b> result on a table
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @return - Returns the <b>FIRST</b> item found encapsulated on {@link Optional} interface based on entityClass type
     * @author victor.bello
     */
    public <T extends BaseEntity> Optional<T> findFirst(Class<T> entityClass) {
        return findFirstOrderedByParams(entityClass, null, null);
    }

    /**
     * Method responsible to retrieve the all results on a table
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @return - All items found or an empty list
     * @see Collections#emptyList()
     */
    public <T extends BaseEntity> List<T> findAll(Class<T> entityClass) {
        return findAllOrderedByParams(entityClass, null, null);
    }

    /**
     * Method responsible to retrieve <b>all</b> results on a table with its result ordered and filtered
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @param predicateClause
     *            - Where condition to filter
     * @param order
     *            - Return ordem of the results. {@link QueryOrder#ASC} will return the result in ascending order. The {@link QueryOrder#DESC} will return the result in descending order.
     * @param columns
     *            - When {@link QueryOrder} is passed by argument, this argument becomes required. This parameter informs the columns which should ordered by
     * @return - All items found or an empty list
     * @author victor.bello
     * @see PredicateClause
     * @see PredicateBuilder
     * @see Collections#emptyList()
     */
    public <T extends BaseEntity> List<T> findAllOrderedByParams(Class<T> entityClass, PredicateClause predicateClause, QueryOrder order, String... columns) {
        EntityManager em = getEm();
        CriteriaQuery<T> cq = generateSelectQuery(em, entityClass, predicateClause, order, columns);
        List<T> resultList = em.createQuery(cq).getResultList();
        return resultList == null ? Collections.emptyList() : resultList;
    }

    /**
     * Method responsible to retrieve <b>all</b> results on a table with its result filtered
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @param predicateClause
     *            - Where condition to filter
     * @return - All items found or an empty list
     * @author victor.bello
     * @see PredicateClause
     * @see PredicateBuilder
     * @see Collections#emptyList()
     */
    public <T extends BaseEntity> List<T> findAllByParams(Class<T> entityClass, PredicateClause predicateClause) {
        return findAllOrderedByParams(entityClass, predicateClause, null);
    }

    /**
     * Method responsible to retrieve <b>all</b> results on a table with its result ordered
     * 
     * @param entityClass
     *            - Entity (table) to be searched.
     * @param order
     *            - Return ordem of the results. {@link QueryOrder#ASC} will return the result in ascending order. The {@link QueryOrder#DESC} will return the result in descending order.
     * @param columns
     *            - When {@link QueryOrder} is passed by argument, this argument becomes required. This parameter informs the columns which should ordered by
     * @return - All items found or an empty list
     * @author victor.bello
     * @see PredicateClause
     * @see PredicateBuilder
     * @see Collections#emptyList()
     */
    public <T extends BaseEntity> List<T> findAllOrdered(Class<T> entityClass, QueryOrder order, String... columns) {
        return findAllOrderedByParams(entityClass, null, order, columns);
    }

    /**
     * Checks if an entity is loaded by JPA
     * 
     * @param entity
     *            - entity to check
     * @return <b>true</b> if is load, <b>false</b> otherwise
     */
    public boolean isLoaded(BaseEntity entity) {
        PersistenceUnitUtil unitUtil = getEm().getEntityManagerFactory().getPersistenceUnitUtil();
        return unitUtil.isLoaded(entity);
    }

    private <T extends BaseEntity> CriteriaQuery<T> generateSelectQuery(EntityManager em, Class<T> entityClass, PredicateClause predicateClause, QueryOrder order, String... columns) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
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

    /**
     * Search by native query using JPA
     * 
     * @param entityClass
     *            - Class to be populate with result
     * @param query
     *            - Native sql query
     * @param params
     *            - values to replace placeholders on query
     * @return - All items found or an empty list
     * @see EntityManager#createNativeQuery(String, Class)
     * @see Collections#emptyList()
     */
    @SuppressWarnings("unchecked")
    protected <T extends BaseEntity> List<T> findByNativeQuery(String query, Class<T> clazz, Object... params) {
        EntityManager em = getEm();
        Query tquery = null;
        if (clazz == null) {
            tquery = em.createNativeQuery(query, clazz);
        } else {
            tquery = em.createNativeQuery(query, clazz);
        }
        int idx = 1;
        for (Object param : params) {
            tquery.setParameter(idx, param);
            idx++;
        }
        List<T> resultList = tquery.getResultList();
        return resultList == null ? Collections.emptyList() : resultList;
    }

    protected <T extends BaseEntity> List<T> findByNativeQuery(String query, Object... params) {
        List<T> resultList = findByNativeQuery(query, null, params);
        return resultList == null ? Collections.emptyList() : resultList;
    }

    protected void removeByNativeQuery(String query, Object... params) {
        EntityManager em = getEm();
        Query tquery = em.createNativeQuery(query);
        int idx = 1;
        for (Object param : params) {
            tquery.setParameter(idx, param);
            idx++;
        }
        tquery.executeUpdate();
    }

    protected void updateByNativeQuery(String query, Object... params) {
        EntityManager em = getEm();
        Query tquery = em.createNativeQuery(query);
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
