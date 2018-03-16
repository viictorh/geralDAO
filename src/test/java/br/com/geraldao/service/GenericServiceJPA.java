package br.com.geraldao.service;

import javax.persistence.EntityManager;

import org.apache.commons.lang.NotImplementedException;

import br.com.geraldao.dao.BaseService;

public class GenericServiceJPA extends BaseService {

    @Override
    protected EntityManager getEm() {
        throw new NotImplementedException("There's no support for JPA implemented");
    }

}
