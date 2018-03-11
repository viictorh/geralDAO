package br.com.geraldao.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.geraldao.dao.QueryExecutor.Result;
import br.com.geraldao.dao.QueryExecutor.ResultType;
import br.com.geraldao.query.ProcedureBuilder;
import br.com.geraldao.query.QueryResultReader;

public abstract class QueryService {
    private final static Logger logger = Logger.getLogger(QueryService.class);

    public QueryService() {
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
        QueryResultReader<T> reader = new QueryResultReader<T>(clazz);
        QueryExecutor queryExecutor = new QueryExecutor(ResultType.ITEM, ProcedureBuilder.procedure(procedure, params), reader);
		try (Connection connection = connection()) {
			Result<T> resultReturn = queryExecutor.execute(connection, clazz);
			return resultReturn.obj;
		}
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
        QueryResultReader<T> reader = new QueryResultReader<T>(clazz);
        QueryExecutor queryExecutor = new QueryExecutor(ResultType.LIST, ProcedureBuilder.procedure(procedure, params), reader);
		try (Connection connection = connection()) {
			Result<T> resultReturn = queryExecutor.execute(connection, clazz);
			return resultReturn.listResult;
		}
    }


}