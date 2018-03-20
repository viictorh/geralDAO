# GeralDAO
Biblioteca utilitária voltada para facilitar a execução de Queries nativas, procedures ou manipulação de entidades JPA.
 
## Pré-requisitos:
- Maven;
- Java 8;

## Adicionando dependência:
 O primeiro passo para adicionar a biblioteca no projeto é inserir no pom.xml do projeto o caminho para o repositório [central da voxage](http://svox-back01:8081/nexus/content/groups/br.com.voxage/):
 
```xml
<repository>
	<id>br.com.voxage</id>
 	<url>http://svox-back01:8081/nexus/content/groups/br.com.voxage/</url>
</repository>
```
Feito isso, agora a biblioteca poderá ser localizada no repositório após adicionada como dependencia

```xml
<dependency>
    <groupId>br.com</groupId>
    <artifactId>geralDAO</artifactId>
    <version>${version}</version>
</dependency>
```

Selecione a versão mais recente disponível no [repositório da voxage](http://svox-back01:8081/nexus/content/groups/br.com.voxage/br/com/geralDAO/).

## Desenvolvimento

### Classes

![Diagrama de classe](https://github.com/viictorh/geralDAO/blob/master/docs/Objects.png?raw=true "Diagrama de classe")

#### JPA

- BaseService - Classe abstrata responsável por realizar as ações relacionadas ao JPA, esta classe estende a classe QueryService.
- PredicateBuilder - Classe responsável por criar as condições (where clause) que irão gerar uma classe PredicateClause. Com ela, pode-se utilizar de forma mais abstrata a geração de filtros de pesquisa em JPA.
- PredicateClause - Gerado pelo predicateBuilder, é uma dependencia do BaseService na utilização de filtros de pesquisa.

#### JDBC

- QueryService - Classe abstrata responsável por conexões JDBC. Com ela pode-se executar procedures (ProcedureBuilder) e queries nativas (QueryBuilder).
- StatementBuilder - Define o que os builders devem implementar para criação de queries. Caso os existentes (ProcedureBuilder e QueryBuilder) não atendam a necessidade, pode-se estende esta classe para utilização no QueryService.
- QueryBuilder - Implementação para criação de queries nativas jdbc como SELECTs, UPDATEs e INSERTs.
- ProcedureBuilder - Implementação para utilização de procedures.
- QueryExecutor - Classe auxiliar na execução dos StatementBuilders
- QueryResultReader - Classe responsável pela leitura do resultado da execução no banco de dados. Com ela pode-se ler o resultado direto em uma classe ou num objeto java especifico lendo apenas uma coluna ou determinada posição.


### Utilização BaseService

  Para utilização dos métodos básicos do JPA o desenvolvedor deverá estender (herdar) a classe "BaseService", com isso, ganha-se também a possibilidade de execução de procedures ou queries nativas.
    Ao estender a classe BaseService, obriga-se a implementação do método getEm(), responsável pela criação do EntityManager. Hoje, na versão 1.0.0-beta, o GeralDAO ainda não lida com EntityManagers do tipo "Resource_Local" apenas "JTA", que tem suas transações gerenciadas pelo servidor (ex: Wildfly).

<p align="center">
<img src="https://github.com/viictorh/geralDAO/blob/master/docs/baservicejpa.png?raw=true" />
</p>

<p align="center">Criando o entityManager para utilização no BaseService</p>

Para utilização da execução de procedures, atualmente existe a implementação default do método connection() do QueryService que retorna uma conexão JDBC, porém esta leitura depende da fornecedor da implementação JPA que está sendo utilizada (Hibernate, EclipseLink, etc), caso durante a obtenção da conexão JDBC ocorra algum erro, deve-se sobrescrever também o método connection().
Ex utilizando hibernate e Wildfly JTA:

<p align="center">
<img src="https://github.com/viictorh/geralDAO/blob/master/docs/queryservice_jdbc_jpa.png?raw=true" />
</p>

<p align="center">Exemplo de utilização com Hibernate, uma vez que necessita-se também a sobrescrita do método connection()</p>

#### Executando métodos

Conforme o diagrama apresentado, com a instancia do BaseService pode-se utilizar diversos métodos de pesquisa, atualização, inserção e deleção, além dos métodos disponíveis no QueryService. Abaixo o exemplo de utilização de alguns:

1 - Listar todos os valores de uma entidade JPA mapeada:

```java
       public void test_findAll() {     
        service = new VirtualAgentJPA();
        List<User> users = service.findAll(User.class);  
        System.out.println(users);
    }
```

2 - Listar todos os valores de uma entidade JPA mapeada, de acordo com filtro:

```java
    public void test_findAllByParam() {
        PredicateClause build = PredicateBuilder.where("login", "victor").build();
        List<User> users = service.findAllByParams(User.class, build);
        System.out.println("Generated query: select <columns> from TUnpbxUser where login = 'victor'");
        System.out.println(users);
    }
```    

3 - Listar todos os valores de uma entidade JPA mapeada, ordenada e de acordo com filtro:

```java
    PredicateClause build = PredicateBuilder.where("login", "victor").andIsNotNull("oldPassword").build();
        List<User> users = service.findAllOrderedByParams(User.class, build, QueryOrder.DESC, "login");
        System.out.println("Generated query: select <columns> from TUnpbxUser where login = 'victor'"
                + " and oldPassword is not null order by login desc");
        System.out.println(users);
    }
```

4 - Listar valores de uma entidade JPA mapeada de acordo com ordenação:

```java
    public void test_findByParamOrdered() {
        PredicateClause block = PredicateBuilder.where("login", Operator.ENDS_WITH, "r").andIsNull("oldPassword").build();
         PredicateClause build = PredicateBuilder.where("login", "victor").andIsNotNull("oldPassword").orBlock(block).build();
        User users = service.findFirstOrderedByParams(User.class, build, QueryOrder.DESC, "login").get();
        System.out.println("Generated query: select <mapped columns> from TUnpbxUser where login = 'victor' and "
                + "oldPassword is not null or (login like '%r' and oldPassword is null)");
    }
```
   
5 - Verificar se uma entidade existe baseada numa condição:

```java
    public void test_entityExists() {
        boolean entityExists = service.entityExists(User.class, PredicateBuilder.whereIsNotNull("login").build());
        System.out.println(entityExists);
    }
```

Para execução dos outros métodos pode-se utilizar o Javadocs (comentários no próprio método), mas a utilização segue os mesmos princípios já exemplificados nos itens anteriores. 


### Utilização QueryService

Caso a utilização do GeralDAO limite-se a execução de procedures ou queries nativas deve-se implementar o método QueryService. Ao implementa-lo, o método connection() deverá ser sobrescrito e retornada uma conexão com o banco de dados.

<p align="center">
<img src="https://github.com/viictorh/geralDAO/blob/master/docs/queryservice.png?raw=true" />
</p>

<p align="center">Criando conexão jdbc direto do datasource após estender a classe QueryService</p>

#### Executando métodos

1 - Executando procedure e obtendo item como resultado já criando a entidade correspondente.

```java
    public void testProcedureWithResult() throws SQLException {
        List<Object> params = Arrays.asList(31, true, true);
        ProcedureDefaultResult findItem = service.findItem(ProcedureBuilder.create("Sp_UnPbxCleanUserConnection",params), ProcedureDefaultResult.class).orElse(new ProcedureDefaultResult());
        System.out.println(findItem);
    }
```

2 -  Executando procedure e obtendo valor de acordo com a posição da coluna no retorno da execução. Exemplo:

Select coluna1, coluna2 from table. A posição 1 retornaria os valores da coluna1:

```java
    public void testProcedureSpecificResultPosition() throws SQLException {
        List<Object> params = Arrays.asList(31, true, true);
        QueryResultReader<String> queryResult = new QueryResultReader<>(String.class, 1);
        String findItem = service.findItem(ProcedureBuilder.create("Sp_UnPbxCleanUserConnection", params),queryResult).get();
        System.out.println(findItem);
    }
```

3 - Executando procedure e obtendo o valor de acordo com o nome da coluna no retorno da execução. Exemplo:

Select coluna1, coluna2 from table

```java
public void testProcedureSpecificResultName() throws SQLException {
        List<Object> params = Arrays.asList(31, true, true);
        QueryResultReader<String> queryResult = new QueryResultReader<>(String.class, "resultDescription");
        String findItem = service.findItem(ProcedureBuilder.create("Sp_UnPbxCleanUserConnection", params),queryResult).get();
        System.out.println(findItem);
    }
```

4 - Utilizando queries nativas em vez de procedure:

```java
public void testQueryFieldDateType() throws SQLException {
        QueryResultReader<Date> queryResult = new QueryResultReader<>(Date.class, "DateInsert");
        List<Date> findItem = service.findAll(QueryBuilder.create("SELECT * FROM TUNPBXUSER"), queryResult);
        System.out.println(findItem);
    }
```

Para execução dos outros métodos pode-se utilizar o Javadocs (comentários no próprio método), mas a utilização segue os mesmos princípios já exemplificados nos itens anteriores.


