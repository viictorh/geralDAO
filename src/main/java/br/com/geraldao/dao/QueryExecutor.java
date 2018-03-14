package br.com.geraldao.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.geraldao.query.QueryResultReader;
import br.com.geraldao.query.StatementBuilder;

/**
 * 
 * @author victor.bello
 *
 */
class QueryExecutor<T> {
    private final Logger         logger = Logger.getLogger(getClass());
    private ResultType           resultType;
    private StatementBuilder     queryBuilder;
    private QueryResultReader<T> queryResultReader;

    protected enum ResultType {
        NONE,
        LIST,
        ITEM;
    }

    QueryExecutor(ResultType resultType, StatementBuilder queryBuilder, QueryResultReader<T> queryResultReader) {
        this.resultType = resultType;
        this.queryBuilder = queryBuilder;
        this.queryResultReader = queryResultReader;
    }

    /**
     * 
     * @param st
     * @param rs
     * @param clazz
     * @param resultType
     * @return
     * @throws SQLException
     * @author victor.bello
     */
    protected Result<T> retrieveResult(PreparedStatement st, ResultSet rs) throws SQLException {
        boolean execute = st.execute();
        Result<T> result = new Result<>(resultType);
        if (resultType == ResultType.NONE) {
            return result;
        }
        RESULT_FIND: while (true && queryResultReader != null) {
            if (execute) {
                rs = st.getResultSet();
                if (rs == null) {
                    break;
                }

                try {
                    while (rs.next()) {
                        T obj = (T) queryResultReader.get(rs);
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

    protected Result<T> execute(Connection connection) throws SQLException {
        Result<T> resultReturn;
        try (PreparedStatement st = connection.prepareStatement(queryBuilder.build()); ResultSet rs = null) {
            queryBuilder.buildStatement(st);
            resultReturn = retrieveResult(st, rs);
        }
        if (resultReturn.lastException != null) {
            throw new SQLException(resultReturn.lastException);
        }
        return resultReturn;
    }

    /**
     * 
     * @author victor.bello
     *
     * @param <T>
     */
    @SuppressWarnings("hiding")
    static class Result<T> {
        protected Exception  lastException;
        protected List<T>    listResult;
        protected T          obj;
        protected ResultType type;

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

    @Override
    public String toString() {
        return "QueryExecutor [resultType=" + resultType + ", queryBuilder=" + queryBuilder + ", queryResultReader=" + queryResultReader + "]";
    }

}
