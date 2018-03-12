package br.com.geraldao.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.com.geraldao.dao.QueryService;

public class GenericService extends QueryService {
    private static final String URL = "jdbc:sqlserver://svox-testew2k12;DatabaseName=Voscenter_Migracao_Banrisul_5_3;user=sa;password=S@voxsql";

    @Override
    protected Connection connection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
