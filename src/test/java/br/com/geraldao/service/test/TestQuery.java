package br.com.geraldao.service.test;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.com.geraldao.bean.ProcedureDefaultResult;
import br.com.geraldao.query.ProcedureBuilder;
import br.com.geraldao.query.QueryBuilder;
import br.com.geraldao.query.QueryResultReader;
import br.com.geraldao.service.GenericService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestQuery {

    private GenericService service;

    @Before
    public void setUp() {
        service = new GenericService();
    }

    @Test
    public void testAProcedureWithResult() throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(31);
        params.add(true);
        params.add(true);
        ProcedureDefaultResult findItem = service.findItem(ProcedureBuilder.create("Sp_UnPbxCleanUserConnection", params), ProcedureDefaultResult.class);
        System.out.println(findItem);
    }

    @Test
    public void testBProcedureSpecificResultPosition() throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(31);
        params.add(true);
        params.add(true);
        QueryResultReader<String> queryResult = new QueryResultReader<>(String.class, 1);
        String findItem = service.findItem(ProcedureBuilder.create("Sp_UnPbxCleanUserConnection", params), queryResult);
        assertTrue(findItem.equals("2"));
        System.out.println(findItem);
    }

    @Test
    public void testCProcedureSpecificResultName() throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(31);
        params.add(true);
        params.add(true);
        QueryResultReader<String> queryResult = new QueryResultReader<>(String.class, "resultDescription");
        String findItem = service.findItem(ProcedureBuilder.create("Sp_UnPbxCleanUserConnection", params), queryResult);
        System.out.println(findItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDProcedureSpecificResultError() throws SQLException {
        System.out.println("Expected exception thrown - IllegalArgumentException");
        List<Object> params = new ArrayList<>();
        params.add(31);
        params.add(true);
        params.add(true);
        QueryResultReader<Integer> queryResult = new QueryResultReader<>(Integer.class);
        Integer findItem = service.findItem(ProcedureBuilder.create("Sp_UnPbxCleanUserConnection", params), queryResult);
        System.out.println(findItem);
    }

    @Test
    public void testEProcedureList() throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(31);
        QueryResultReader<Integer> queryResult = new QueryResultReader<>(Integer.class, 5);
        List<Integer> findItem = service.findAll(ProcedureBuilder.create("SP_Campanhas_Ativas", params), queryResult);
        System.out.println(findItem);
    }

    @Test
    public void testFQueryField() throws SQLException {
        QueryResultReader<Integer> queryResult = new QueryResultReader<>(Integer.class, 1);
        Integer findItem = service.findItem(QueryBuilder.create("SELECT * FROM TUNPBXUSER WHERE IDUser = ?", Arrays.asList(31)), queryResult);
        System.out.println(findItem);
    }

    @Test
    public void testGQueryFieldDateType() throws SQLException {
        QueryResultReader<Date> queryResult = new QueryResultReader<>(Date.class, "ENDOFLASTATTENDANCE");
        Date findItem = service.findItem(QueryBuilder.create("SELECT * FROM TUNPBXUSER WHERE IDUser = ?", Arrays.asList(31)), queryResult);
        System.out.println(findItem);
    }

    @Test
    public void testHQueryFieldDateType() throws SQLException {
        QueryResultReader<Date> queryResult = new QueryResultReader<>(Date.class, "DateInsert");
        List<Date> findItem = service.findAll(QueryBuilder.create("SELECT * FROM TUNPBXUSER"), queryResult);
        System.out.println(findItem);
    }

    @Test
    public void testIQueryFieldDateType() throws SQLException {
        QueryResultReader<Integer> queryResult = new QueryResultReader<>(Integer.class, "IDUser");
        Integer findItem = service.findItem(QueryBuilder.create("SELECT * FROM TUNPBXUSER"), queryResult);
        System.out.println(findItem);
    }

}
