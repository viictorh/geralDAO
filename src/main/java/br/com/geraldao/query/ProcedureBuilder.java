package br.com.geraldao.query;

import java.util.List;

import br.com.geraldao.dao.QueryService;
import br.com.geraldao.util.ListUtil;

/**
 * 
 * @author victor.bello
 * @see StatementBuilder
 */
public class ProcedureBuilder extends StatementBuilder {

    private ProcedureBuilder() {
    }

    private ProcedureBuilder(String procedure) {
        this.query = procedure;
    }

    private ProcedureBuilder(String procedure, List<?> parameters) {
        this(procedure);
        this.parameters = parameters;
    }

    /**
     * Defines a procedure to be execute.
     * 
     * @param procedure
     *            name, EG: SP_FindAll_Users, USP_FindAll_Groups
     * @return ProcedureBuilder Object
     * @see StatementBuilder
     * @see QueryService
     * @author victor.bello
     */
    public static ProcedureBuilder create(String procedure) {
        return new ProcedureBuilder(procedure);
    }

    /**
     * Defines a procedure to be execute.
     * 
     * @param procedure
     *            name, EG: SP_FindAll_Users, USP_FindAll_Groups
     * @param parameters
     *            List of parameters to be set on procedure
     * @return ProcedureBuilder Object
     * @see StatementBuilder
     * @see QueryService
     * @author victor.bello
     */
    public static ProcedureBuilder create(String procedure, List<?> parameters) {
        return new ProcedureBuilder(procedure, parameters);
    }

    /**
     * Build {@code String} to be used on {@code PreparedStatement} according to parameters and procedure
     * 
     * @see QueryService
     * @author victor.bello
     */
    @Override
    public String build() {
        StringBuilder sb = new StringBuilder("{call " + query + "(");
        if (!ListUtil.isCollectionEmpty(parameters)) {
            String comma = "";
            for (int i = 0; i < parameters.size(); i++) {
                sb.append(comma).append("?");
                comma = ",";
            }
        }
        sb.append(")}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ProcedureBuilder [" + (parameters != null ? "parameters=" + parameters + ", " : "") + (query != null ? "query=" + query : "") + "]";
    }

}
