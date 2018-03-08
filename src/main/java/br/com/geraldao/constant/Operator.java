package br.com.geraldao.constant;

/**
 * SQL Operators to define which where clause use.
 * <ul>
 * <li>{@link #EQUAL}</li>
 * <li>{@link #NOT_EQUAL}</li>
 * <li>{@link #LESS_THAN_OR_EQUAL}</li>
 * <li>{@link #STARTS_WITH}</li>
 * <li>{@link #ENDS_WITH}</li>
 * <li>{@link #CONTAINS}</li>
 * <li>{@link #IN}</li>
 * <li>{@link #NOT_IN}</li>
 * <li>{@link #NOT_NULL}</li>
 * <li>{@link #ISNULL}</li>
 * </ul>
 * 
 * @author victor.bello
 *
 */
public enum Operator {
    /**
     * Indicates that where clause should check for equality. EG: SELECT * FROM Table WHERE id = fieldValue
     */
    EQUAL,
    /**
     * Indicates that where clause should check whether the field starts with the field value. EG: SELECT * FROM Table WHERE name LIKE 'fieldValue%'
     */
    STARTS_WITH,
    /**
     * Indicates that where clause should check whether the field ends with the field value. EG: SELECT * FROM Table WHERE name LIKE '%fieldValue'
     */
    ENDS_WITH,
    /**
     * Indicates that where clause should check whether the field contains the field value. EG: SELECT * FROM Table WHERE name LIKE '%fieldValue%'
     */
    CONTAINS,
    /**
     * Indicates that where clause should check whether field is contained in a list of values. EG: SELECT * FROM Table WHERE id IN(1,2,3,4,5)
     */
    IN,
    /**
     * Indicates that where clause should check whether field is not contained in a list of values. EG: SELECT * FROM Table WHERE id NOT IN(1,2,3,4,5)
     */
    NOT_IN,
    /**
     * Indicates that where clause should check whether field is less or equal to the field value. EG: SELECT * FROM Table WHERE id <> fieldValue
     */
    LESS_THAN_OR_EQUAL,
    /**
     * Indicates that where clause should check for inequality. EG: SELECT * FROM Table WHERE id IS NOT NULL
     */
    NOT_EQUAL,
    /**
     * Indicates that where clause should check for not null field. EG: SELECT * FROM Table WHERE id IS NOT NULL
     */
    NOT_NULL,
    /**
     * Indicates that where clause should check for null. EG: SELECT * FROM Table WHERE id IS NULL
     */
    ISNULL;
}
