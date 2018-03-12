package br.com.geraldao.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;

import br.com.geraldao.annotation.Ignore;
import br.com.geraldao.exception.ANIMALTypeException;

/**
 * Class responsible for reading {@link ResultSet} from executed Statement <br>
 * <br>
 * If an mapped object from resultSet will be use, {@link QueryResultReader#QueryResultReader(Class)} is the way.<br>
 * If you want just one column from resultSet by its position, {@link QueryResultReader#QueryResultReader(Class, int)} is the way.<br>
 * If you want just one column from resultSet by its column name, {@link QueryResultReader#QueryResultReader(Class, String)} is the way.<br>
 * 
 * @author victor.bello
 *
 * @param <T>
 *            Class type result
 */
public class QueryResultReader<T> {

    private enum DataType {
        /**
         * java object which represents the return of resultSet.
         */
        COMPLEX_OBJECT,
        /**
         * Java object wrapper or default classes as {@link Integer, Double, String, Date}, etc which will be found on resultSet by column name
         */
        WRAPPER_BY_NAME,
        /**
         * Java object wrapper or default classes as {@link Integer, Double, String, Date}, etc which will be found on resultSet by column position
         */
        WRAPPER_BY_POSITION
    }

    private DataType dataType;
    private Class<T> clazz;
    private String   parameterName;
    private Integer  position;

    /**
     * Creates a QueryResultReader.<br>
     * This class <b>must not be</b> one of <b>Primitives Wrappers</b> classes, {@link String}, children classes of {@link Number}, {@link Date} or parent themselves. If it is, an {@link IllegalArgumentException} will be thrown
     * 
     * @param clazz
     *            Complex object data type.
     * @author victor.bello
     */
    public QueryResultReader(Class<T> clazz) {
        if (!isComplexClass(clazz)) {
            throw new IllegalArgumentException("Invalid parameter. This class should be use indicating parameterName or position of the resultSet returning");
        }
        this.clazz = clazz;
        this.dataType = DataType.COMPLEX_OBJECT;
    }

    /**
     * Creates a QueryResultReader.<br>
     * This class <b>must be</b> of java primitive wrappers, {@link String}, children classes of {@link Number}, {@link Date} or parent themselves. If it isn't, an {@link IllegalArgumentException} will be thrown
     * 
     * @param clazz
     *            - Java primitive wrapper
     * @param parameterName
     *            column <b>name</b> to be seek on resultSet
     * @author victor.bello
     */
    public QueryResultReader(Class<T> clazz, String parameterName) {
        if (isComplexClass(clazz)) {
            throw new IllegalArgumentException("Invalid parameter. This class probably mapped the resultSet, you must not pass which parameter you want");
        }
        this.clazz = clazz;
        this.dataType = DataType.WRAPPER_BY_NAME;
        this.parameterName = parameterName;
    }

    /**
     * Creates a QueryResultReader.<br>
     * This class <b>must be</b> of java primitive wrappers, {@link String}, children classes of {@link Number}, {@link Date} or parent themselves. If it isn't, an {@link IllegalArgumentException} will be thrown
     * 
     * @param clazz
     *            - Java primitive wrapper
     * @param position
     *            - column <b>position</b> to be seek on resultSet. This value <i>IS NOT 0 BASED</i>, so it starts from <b>1</b>
     * @author victor.bello
     */
    public QueryResultReader(Class<T> clazz, int position) {
        if (isComplexClass(clazz)) {
            throw new IllegalArgumentException("Invalid parameter. This class probably mapped the resultSet, you must not pass which parameter position you want");
        }
        this.clazz = clazz;
        this.dataType = DataType.WRAPPER_BY_POSITION;
        this.position = position;
    }

    private boolean isComplexClass(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class should not be null. Check usage for the correct method");
        }
        return !(Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz));
    }

    /**
     * Read resultSet according to QueryResultReader creation
     * 
     * @param rs
     *            ResultSet
     * @return Object found on resultSet
     * @throws SQLException
     * @author victor.bello
     */
    public T get(ResultSet rs) throws SQLException {
        switch (dataType) {
            case COMPLEX_OBJECT:
                return readByReflection(clazz, rs);
            case WRAPPER_BY_NAME:
                return readByReflection(clazz, rs, parameterName);
            case WRAPPER_BY_POSITION:
                return readByReflection(clazz, rs, position);
        }
        return null;
    }

    /**
     * 
     * @param clazz
     * @param rs
     * @return
     * @throws SQLException
     * @author victor.bello
     */
    @SuppressWarnings("unchecked")
    private T readByReflection(Class<T> clazz, ResultSet rs, Object rsParam) throws SQLException {
        if (rsParam instanceof String) {
            String param = (String) rsParam;
            return (T) convertTypes(rs.getObject(param), clazz);
        } else {
            Integer param = (Integer) rsParam;
            return (T) convertTypes(rs.getObject(param), clazz);
        }
    }

    /**
     * 
     * @param clazz
     * @param rs
     * @return
     * @throws SQLException
     * @author victor.bello e yuri.campolongo
     */
    @SuppressWarnings("unchecked")
    private T readByReflection(Class<T> clazz, ResultSet rs) throws SQLException {
        try {
            T obj = (T) Class.forName(clazz.getName()).newInstance();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                Annotation annotation = method.getAnnotation(Column.class);
                Annotation ignoreColumn = method.getAnnotation(Ignore.class);
                if (ignoreColumn == null && annotation != null) {
                    Class<Column> type = (Class<Column>) annotation.annotationType();
                    Method met = type.getDeclaredMethod("name");
                    Object value = convertTypes(rs.getObject(met.invoke(annotation).toString()), method.getReturnType());
                    if (value != null) {
                        Method declaredMethod = obj.getClass().getMethod(method.getName().replace("get", "set"), value.getClass());
                        declaredMethod.invoke(obj, value);
                    }
                }
            }

            return obj;

        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            throw new ANIMALTypeException("Method not found " + e.getMessage() + " please check in your mapping procedure class if the parameter type is the same as the procedure returning type");
        } catch (IllegalArgumentException | SecurityException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new ANIMALTypeException("Please make sure all your methods in procedure mapping with @colum annotation have public access");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ForName was not able to identify your class " + clazz.getName());
        }
    }

    /**
     * Convert database types to java types
     * 
     * @param value
     *            the value returned by database
     * @param type
     *            expected by method
     * @return converted object
     */
    protected Object convertTypes(Object value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isAssignableFrom(type)) {
            return value;
        }
        if (Boolean.class.isAssignableFrom(type)) {
            if (value instanceof Number && ((Number) value).intValue() != 0) {
                return true;
            } else {
                return false;
            }
        }
        if (value instanceof Timestamp) {
            if (Timestamp.class.isAssignableFrom(type)) {
                return (Timestamp) value;
            }
            return new Date(((Timestamp) value).getTime());
        }
        if (value instanceof Number) {
            if (Long.class.isAssignableFrom(type)) {
                return ((Number) value).longValue();
            }
            if (Integer.class.isAssignableFrom(type)) {
                return ((Number) value).intValue();
            }
            if (Float.class.isAssignableFrom(type)) {
                return ((Number) value).floatValue();
            }
            if (Double.class.isAssignableFrom(type)) {
                return ((Number) value).doubleValue();
            }
            if (Byte.class.isAssignableFrom(type)) {
                return ((Number) value).byteValue();
            }
            if (Short.class.isAssignableFrom(type)) {
                return ((Number) value).shortValue();
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "QueryResultReader [" + (dataType != null ? "dataType=" + dataType + ", " : "") + (clazz != null ? "clazz=" + clazz + ", " : "") + (parameterName != null ? "parameterName=" + parameterName + ", " : "") + (position != null ? "position=" + position : "") + "]";
    }

}
