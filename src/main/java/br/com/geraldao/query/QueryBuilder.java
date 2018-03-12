package br.com.geraldao.query;

import java.sql.PreparedStatement;
import java.util.List;

import br.com.geraldao.dao.QueryService;

/**
 * 
 * @author victor.bello
 * @see StatementBuilder
 */
public class QueryBuilder extends StatementBuilder {

    private QueryBuilder() {
    }

    private QueryBuilder(String query) {
        this.query = query;
    }

    private QueryBuilder(String query, List<?> parameters) {
        this(query);
        this.parameters = parameters;
    }

    /**
     * Defines a query to be executed
     * 
     * @param query
     *            must be passed already formated to {@link PreparedStatement}. EG: {@code SELECT * FROM TableName}
     * @return QueryBuilder Object
     * @see StatementBuilder
     * @see QueryService
     * @author victor.bello
     */
    public static QueryBuilder create(String query) {
        return new QueryBuilder(query);
    }

    /**
     * Defines a query to be executed
     * 
     * @param query
     *            must be passed already formated to {@link PreparedStatement}. This way, parameter values should be changed to placeholder '?' <br>
     *            EG: {@code SELECT * FROM TableName where ID = ?, NAME = ?}
     * @return QueryBuilder Object
     * @see StatementBuilder
     * @see QueryService
     * @author victor.bello
     */
    public static QueryBuilder create(String query, List<?> parameters) {
        return new QueryBuilder(query, parameters);
    }

    @Override
    public String build() {
        return query;
    }

    @Override
    public String toString() {
        return "QueryBuilder [" + (parameters != null ? "parameters=" + parameters + ", " : "") + (query != null ? "query=" + query : "") + "]";
    }

}
