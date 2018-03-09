package br.com.geraldao.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;

import org.apache.log4j.Logger;

import br.com.geraldao.annotation.Ignore;
import br.com.geraldao.exception.ANIMALTypeException;
import br.com.geraldao.util.ListUtil;

public abstract class ProceduresService {
    private final static Logger logger = Logger.getLogger(ProceduresService.class);

    private enum ResultType {
        LIST,
        ITEM
    }

    public ProceduresService() {
    }

    /**
     * Create an open connection with database. This connection will be closed after used.
     * 
     * @return JDBC Connection
     */
    protected abstract Connection connection();

    /**
     * Executa procedure sem leitura do retorno
     * 
     * @param procedure
     *            - Procedure que será executada
     * @param params
     *            - Parametros a serem enviados para procedure (em ordem)
     * @throws SQLException
     * @author victor.bello
     */
    public void executeProcedure(final String procedure, final List<?> params) throws SQLException {
        findByProcedure(procedure, params, null);
    }

    /**
     * Executa procedure sem leitura do retorno
     * 
     * @param procedure
     *            - Procedure que será executada sem parametros
     * @throws SQLException
     * @author victor.bello
     */
    public void executeProcedure(final String procedure) throws SQLException {
        findByProcedure(procedure, null, null);
    }

    /**
     * Retorna o primeiro objeto a partir da execução da procedure
     * 
     * @param procedure
     *            - Procedure que será executada sem parametros
     * @param clazz
     *            - Classe da qual o objeto será criado
     * @return - Retorno do objeto a partir da classe solicitada
     * @throws SQLException
     * @author victor.bello
     */
    public <T> T findByProcedure(final String procedure, final Class<T> clazz) throws SQLException {
        return findByProcedure(procedure, null, clazz);
    }

    /**
     * Retorna o primeiro objeto a partir da execução da procedure
     * 
     * @param procedure
     *            - Procedure que será executada
     * @param params
     *            - Parametros necessários para a procedure (na ordem)
     * @param clazz
     *            - Classe de resultado para procedure
     * @return - Objeto da classe passada
     * @throws SQLException
     * @author victor.bello
     */
    public <T> T findByProcedure(final String procedure, final List<?> params, final Class<T> clazz) throws SQLException {
        logger.debug("Procedure: " + procedure);
        logger.debug("Param: " + params);
        Result<T> resultReturn = execute(procedure, params, clazz, ResultType.ITEM);
        return resultReturn.obj;
    }

    /**
     * Generic implementation to execute dynamic procedures passing parameters and a class type to be returned in a List.
     * 
     * @param procedure
     *            name of Procedure to be executed without '?' as parameters
     * @param params
     *            to be put in a procedure filter
     * @param clazz
     *            class type that will be returned in a list of that type
     * @return ResultSet with the return of the database
     * @throws SQLException
     */
    public <T> List<T> findAllByProcedure(final String procedure, final List<?> params, final Class<T> clazz) throws SQLException {
        logger.debug("Procedure: " + procedure);
        logger.debug("Param: " + params);
        Result<T> resultReturn = execute(procedure, params, clazz, ResultType.LIST);
        return resultReturn.listResult;
    }

    private <T> Result<T> execute(final String procedure, final List<?> params, final Class<T> clazz, ResultType resultType) throws SQLException {
        Result<T> resultReturn;
        final String builtProcedure = buildProcedure(procedure, params);
        try (Connection connection = connection(); CallableStatement st = connection.prepareCall(builtProcedure); ResultSet rs = null) {
            addParamsToProcedure(params, st);
            resultReturn = retrieveResult(st, rs, clazz, resultType);
        }
        if (resultReturn.lastException != null) {
            throw new SQLException(resultReturn.lastException);
        }
        return resultReturn;
    }

    /**
     * Build a procedure to be executed
     * 
     * @param procedure
     *            name of procedure to be built
     * @param qtdParams
     *            the quantity of paramns this procedure needs
     * @return formatted procedure to be executed by JDBC
     * @throws SQLException
     */
    private String buildProcedure(String procedure, List<?> params) throws SQLException {
        StringBuilder sb = new StringBuilder("{call " + procedure + "(");
        if (!ListUtil.isCollectionEmpty(params)) {
            String comma = "";
            for (int i = 0; i < params.size(); i++) {
                sb.append(comma).append("?");
                comma = ",";
            }
        }
        sb.append(")}");
        return sb.toString();
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
    private Object convertTypes(Object value, Class<?> type) {
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

    /**
     * 
     * @param st
     * @param rs
     * @param clazz
     * @param item
     * @return
     * @throws SQLException
     * @author victor.bello
     */
    private <T> Result<T> retrieveResult(CallableStatement st, ResultSet rs, Class<T> clazz, ResultType item) throws SQLException {
        boolean execute = st.execute();
        Result<T> result = new Result<>(item);
        RESULT_FIND: while (true && clazz != null) {
            if (execute) {
                rs = st.getResultSet();
                if (rs == null) {
                    break;
                }

                try {
                    while (rs.next()) {
                        T obj = readByReflection(clazz, rs);
                        if (result.addAndStop(obj)) {
                            break RESULT_FIND;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    execute = st.getMoreResults();
                    result.lastException = e;
                    continue;
                }
                break;
            } else {
                int updateCount = st.getUpdateCount();
                if (updateCount == -1) {
                    break;
                }
                execute = st.getMoreResults();
            }
        }
        return result;
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
    private <T> T readByReflection(Class<T> clazz, ResultSet rs) throws SQLException {
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

    private void addParamsToProcedure(List<?> params, CallableStatement st) throws SQLException {
        int pos = 1;
        if (!ListUtil.isCollectionEmpty(params)) {
            for (Object o : params) {
                if (o instanceof Date) {
                    st.setDate(pos++, new java.sql.Date(((Date) o).getTime()));
                } else {
                    st.setObject(pos++, o);
                }
            }
        }
    }

    /**
     * 
     * @author victor.bello
     *
     * @param <T>
     */
    private class Result<T> {
        public Exception   lastException;
        private List<T>    listResult;
        private T          obj;
        private ResultType type;

        public Result(ResultType type) {
            this.type = type;
            if (type == ResultType.LIST) {
                listResult = new ArrayList<>();
            }
        }

        public boolean addAndStop(T value) {
            boolean stop = false;
            if (type == ResultType.LIST) {
                listResult.add(value);
            } else {
                obj = value;
                stop = true;
            }
            lastException = null;
            return stop;
        }
    }

}