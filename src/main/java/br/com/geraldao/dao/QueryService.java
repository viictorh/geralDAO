package br.com.geraldao.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.geraldao.dao.QueryExecutor.Result;
import br.com.geraldao.dao.QueryExecutor.ResultType;
import br.com.geraldao.query.ProcedureBuilder;
import br.com.geraldao.query.QueryBuilder;
import br.com.geraldao.query.QueryResultReader;
import br.com.geraldao.query.StatementBuilder;

/**
 * Provides an interface to connect and execute queries or procedures on database
 * 
 * @author victor.bello
 *
 */
public abstract class QueryService {
    private final static Logger logger = Logger.getLogger(QueryService.class);

    public QueryService() {
    }

    /**
     * Create an open connection with database. This connection will be closed after used.
     * 
     * @return JDBC Connection
     * @author victor.bello
     */
    protected abstract Connection connection();

    /**
     * Executes a query or procedure and returns its result based on clazz parameter.
     * 
     * @param builder
     *            Class which extends {@link StatementBuilder}. This class is used to create statements and execute it accordingly.
     * @param clazz
     *            which is not one of <b>Primitives Wrappers</b> classes, {@link String.class}, children classes of {@link Number} or {@link Date} or themselves
     * @return Create Object based on class passed by parameter
     * @throws SQLException
     * @author victor.bello
     * @see {@link QueryResultReader}
     */
    public <T> T findItem(StatementBuilder builder, Class<T> clazz) throws SQLException {
        return (T) findItem(builder, new QueryResultReader<T>(clazz));
    }

    /**
     * Executes a query or procedure and returns its result based on {@code QueryResultReader}.
     * 
     * @param builder
     *            Class which extends {@link StatementBuilder}. This class is used to create statements and execute it accordingly.
     * @param reader
     *            defines how to read {@code ResultSet} return. It can be read to a Object class or a single class return as String, Integer, etc.
     * @return
     * @throws SQLException
     * @author victor.bello
     * @see {@link QueryResultReader}
     */
    public <T> T findItem(StatementBuilder builder, QueryResultReader<T> reader) throws SQLException {
        Result<T> resultReturn = execute(builder, reader, ResultType.ITEM);
        return resultReturn.obj;
    }

    /**
     * Executes a query or procedure and returns a {@link List} of parameter clazz.
     * 
     * @param builder
     *            Class which extends {@link StatementBuilder}. This class is used to create statements and execute it accordingly.
     * @param clazz
     *            which is not one of <b>Primitives Wrappers</b> classes, {@link String.class}, children classes of {@link Number} or {@link Date} or themselves
     * @return Create Object based on class passed by parameter
     * @throws SQLException
     * @author victor.bello
     * @see {@link QueryResultReader}
     */
    public <T> List<T> findAll(StatementBuilder builder, Class<T> clazz) throws SQLException {
        return findAll(builder, new QueryResultReader<T>(clazz));
    }

    /**
     * Executes a query or procedure and returns a {@link List} of parameter clazz defined on {@link QueryResultReader}.
     * 
     * @param builder
     *            Class which extends {@link StatementBuilder}. This class is used to create statements and execute it accordingly.
     * @param clazz
     *            which is not one of <b>Primitives Wrappers</b> classes, {@link String.class}, children classes of {@link Number} or {@link Date} or themselves
     * @return Create Object based on class passed by parameter
     * @throws SQLException
     * @author victor.bello
     * @see {@link QueryResultReader}
     */
    public <T> List<T> findAll(StatementBuilder builder, QueryResultReader<T> reader) throws SQLException {
        Result<T> resultReturn = execute(builder, reader, ResultType.LIST);
        return resultReturn.listResult;
    }

    /**
     * Executes a query or procedure without reading its result.
     * 
     * @param reader
     *            defines how to read {@code ResultSet} return. It can be read to a Object class or a single class return as String, Integer, etc.
     * @throws SQLException
     * @see {@link ProcedureBuilder}
     * @see {@link QueryBuilder}
     */
    public void execute(StatementBuilder builder) throws SQLException {
        execute(builder, (QueryResultReader<?>) null, ResultType.NONE);
    }

    private <T> Result<T> execute(StatementBuilder builder, QueryResultReader<T> reader, ResultType resultType) throws SQLException {
        QueryExecutor<T> queryExecutor = new QueryExecutor<T>(resultType, builder, reader);
        logger.debug(queryExecutor);
        try (Connection connection = connection()) {
            return queryExecutor.execute(connection);
        }
    }

}