package br.com.geraldao.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import br.com.geraldao.util.ListUtil;

public class ProcedureBuilder implements QueryBuilder {

	private String procedure;
	private List<?> parameters;

	private ProcedureBuilder() {
	}

	private ProcedureBuilder(String procedure) {
		this.procedure = procedure;
	}

	private ProcedureBuilder(String procedure, List<?> parameters) {
		this(procedure);
		this.parameters = parameters;
	}

	public static ProcedureBuilder procedure(String procedure) {
		return new ProcedureBuilder(procedure);
	}

	public static ProcedureBuilder procedure(String procedure, List<?> parameters) {
		return new ProcedureBuilder(procedure, parameters);
	}

	@Override
	public String build() {
		StringBuilder sb = new StringBuilder("{call " + procedure + "(");
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

}
