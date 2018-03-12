package br.com.geraldao.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import br.com.geraldao.util.ListUtil;

/**
 * Generate preparedStatement based on query and parameters.
 * 
 * @author victor.bello
 *
 */
public abstract class StatementBuilder {

    protected List<?> parameters;
    protected String  query;

    /**
     * Build string query to be consumed by {@code preparedStatement}
     * 
     * @return
     */
    public abstract String build();

    /**
     * Defines {@code PreparedStatement} parameters if required.
     * 
     * @param st
     *            - {@link PreparedStatement}
     * @return - {@link PreparedStatement} with parameters set.
     * @throws SQLException
     */
    public PreparedStatement buildStatement(PreparedStatement st) throws SQLException {
        int pos = 1;
        if (!ListUtil.isCollectionEmpty(parameters)) {
            for (Object o : parameters) {
                if (o instanceof Date) {
                    st.setDate(pos++, new java.sql.Date(((Date) o).getTime()));
                } else {
                    st.setObject(pos++, o);
                }
            }
        }
        return st;
    }

    @Override
    public String toString() {
        return "StatementBuilder [" + (parameters != null ? "parameters=" + parameters + ", " : "") + (query != null ? "query=" + query : "") + "]";
    }

}
