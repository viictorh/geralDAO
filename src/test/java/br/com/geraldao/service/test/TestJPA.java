package br.com.geraldao.service.test;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Before;

import br.com.geraldao.constant.Operator;
import br.com.geraldao.constant.QueryOrder;
import br.com.geraldao.entity.User;
import br.com.geraldao.predicate.PredicateBuilder;
import br.com.geraldao.predicate.PredicateClause;
import br.com.geraldao.service.GenericServiceJPA;

public class TestJPA {

    private GenericServiceJPA service;

    @Before
    public void setUp() {
        service = new GenericServiceJPA();
    }

    public void test_findAll() {
        List<User> users = service.findAll(User.class);
        assertThat(users, CoreMatchers.instanceOf(Collections.class));
        System.out.println(users);
    }

    public void test_findAllByParam() {
        PredicateClause build = PredicateBuilder.where("login", "victor").build();
        List<User> users = service.findAllByParams(User.class, build);
        assertThat(users, CoreMatchers.instanceOf(Collections.class));
        System.out.println("Generated query: select <columns> from TUnpbxUser where login = 'victor'");
        System.out.println(users);
    }

    public void test_findAllByParamOrdered() {
        PredicateClause build = PredicateBuilder.where("login", "victor").andIsNotNull("oldPassword").build();
        List<User> users = service.findAllOrderedByParams(User.class, build, QueryOrder.DESC, "login");
        assertThat(users, CoreMatchers.instanceOf(Collections.class));
        System.out.println("Generated query: select <columns> from TUnpbxUser where login = 'victor' and oldPassword is not null");
        System.out.println(users);
    }

    public void test_findByParamOrdered() {
        PredicateClause block = PredicateBuilder.where("login", Operator.ENDS_WITH, "r").andIsNull("oldPassword").build();
        PredicateClause build = PredicateBuilder.where("login", "victor").andIsNotNull("oldPassword").orBlock(block).build();
        Class<User> entityClass = User.class;
        User users = service.findFirstOrderedByParams(entityClass, build, QueryOrder.DESC, "login");
        System.out.println("Generated query: select <mapped columns> from TUnpbxUser where login = 'victor' and oldPassword is not null or (login like '%r' and oldPassword is null)");
        assertThat(users, CoreMatchers.instanceOf(entityClass));
        System.out.println(users);
    }

    public void SHOULD_NOT_THROW_NULLPOINTER() {
        PredicateClause build = PredicateBuilder.where("login", Operator.IN, Arrays.asList("-1", "-2", "any other string")).build();
        List<User> users = service.findAllOrderedByParams(User.class, build, QueryOrder.DESC, "login");
        System.out.println("Generated query: select <mapped columns> from TUnpbxUser where login not in ('-1','-2','any other string')");
        System.out.println("If condition return nothing, should not throw nullpointer when using list returned. Instead, an immutable list will be returned");
        System.out.println("Is user list null? -" + users.isEmpty());
        assertThat(users, CoreMatchers.instanceOf(Collections.class));
    }

}
