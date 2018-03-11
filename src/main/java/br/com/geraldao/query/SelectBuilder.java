package br.com.geraldao.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import br.com.geraldao.util.ListUtil;

public class SelectBuilder implements QueryBuilder {

	private String columns;
	private String fromTable;
	private List<?> where;
	private String orderBy;
	private String groupBy;

	private SelectBuilder() {
	}

	private SelectBuilder(String columns, String fromTable) {
		this.columns = columns;
		this.fromTable = fromTable;
	}

	private SelectBuilder(String columns, String fromTable, List<?> where) {
		this(columns, fromTable);
		this.where = where;
	}

	public static SelectBuilder select(String columns, String fromTable) {
		return new SelectBuilder(columns, fromTable);
	}

	public static SelectBuilder select(String columns, String fromTable, List<?> where) {
		return new SelectBuilder(columns, fromTable, where);
	}

	@Override
	public String build() {
		StringBuilder sb = new StringBuilder("SELECT ");
		sb.append(columns).append(" FROM").append(fromTable);
		if (!ListUtil.isCollectionEmpty(where)) {
			String comma = "";
			for (int i = 0; i < where.size(); i++) {
				sb.append(comma).append("?");
				comma = ",";
			}
		}

		if (groupBy != null) {
			sb.append(" GROUP BY ").append(groupBy);
		}

		if (orderBy != null) {
			sb.append(" ORDER BY ").append(orderBy);
		}

		return null;
	}

	@Override
	public PreparedStatement buildStatement(PreparedStatement st) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


}
