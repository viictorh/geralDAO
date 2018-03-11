package br.com.geraldao.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.geraldao.query.QueryBuilder;
import br.com.geraldao.query.QueryResultReader;

class QueryExecutor {
	private final Logger logger = Logger.getLogger(getClass());
	private ResultType item;
	private QueryBuilder queryBuilder;
	private QueryResultReader<?> queryResultReader;

	enum ResultType {
		LIST, ITEM;
	}

	public QueryExecutor(ResultType item, QueryBuilder queryBuilder, QueryResultReader<?> queryResultReader) {
		this.item = item;
		this.queryBuilder = queryBuilder;
		this.queryResultReader = queryResultReader;
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
	protected <T> Result<T> retrieveResult(PreparedStatement st, ResultSet rs, Class<T> clazz) throws SQLException {
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
						@SuppressWarnings("unchecked")
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

	protected <T> Result<T> execute(Connection connection, Class<T> clazz) throws SQLException {
		Result<T> resultReturn;
		try (PreparedStatement st = connection.prepareCall(queryBuilder.build()); ResultSet rs = null) {
			queryBuilder.buildStatement(st);
			resultReturn = retrieveResult(st, rs, clazz);
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
	class Result<T> {
		protected Exception lastException;
		protected List<T> listResult;
		protected T obj;
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
}
