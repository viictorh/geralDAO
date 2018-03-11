package br.com.geraldao.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryBuilder {

	String build();

	PreparedStatement buildStatement(PreparedStatement st) throws SQLException;
}
