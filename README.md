# Dropwizard JDBI Bundle

[![Build Status](https://travis-ci.org/dropwizard/dropwizard-jdbi.svg?branch=master)](https://travis-ci.org/dropwizard/dropwizard-jdbi)
[![Coverage Status](https://img.shields.io/coveralls/dropwizard/dropwizard-jdbi.svg)](https://coveralls.io/r/dropwizard/dropwizard-jdbi)
[![Maven Central](https://img.shields.io/maven-central/v/io.dropwizard.modules/dropwizard-jdbi.svg)](http://mvnrepository.com/artifact/io.dropwizard.modules/dropwizard-jdbi)

The `dropwizard-jdbi` module provides you with managed access to [JDBI], a flexible and modular library for interacting with relational databases via SQL.

The package adds support for [Guava] and [Joda-Time] classes in JDBI, integrates logging via [Logback], and provides [Jersey] exception mappers.

### Deprecation note

It is recommended that new projects use the `dropwizard-jdbi3` module.
Existing projects can update by following the [Jdbi 3 migration guide].


[JDBI]: http://jdbi.org/jdbi2/
[Dropwizard]: http://dropwizard.io/
[Guava]: https://github.com/google/guava
[Joda-Time]: https://www.joda.org/joda-time/
[Logback]: https://logback.qos.ch/
[Jersey]: https://projects.eclipse.org/projects/ee4j.jersey
[Jdbi 3 migration guide]: http://jdbi.org/#_upgrading_from_v2_to_v3

## Maven Artifacts

This project is available on Maven Central. To add it to your project you can add the following dependencies to your
`pom.xml`:

    <dependency>
      <groupId>io.dropwizard.modules</groupId>
      <artifactId>dropwizard-jdbi</artifactId>
      <version>${dropwizard.version}</version>
    </dependency>


## Configuration

To create a managed, instrumented `DBI` instance, your configuration class needs a `DataSourceFactory` instance:

```java
public class ExampleConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
}
```

Then, in your service's `run` method, create a new `DBIFactory`:

```java
@Override
public void run(ExampleConfiguration config, Environment environment) {
    final DBIFactory factory = new DBIFactory();
    final DBI jdbi = factory.build(environment, config.getDataSourceFactory(), "postgresql");
    final UserDAO dao = jdbi.onDemand(UserDAO.class);
    environment.jersey().register(new UserResource(dao));
}
```

This will create a new managed connection pool to the database, a health check for connectivity to the database, and a new `DBI` instance for you to use.

Your service's configuration file will then look like this:

```yaml
database:
  # the name of your JDBC driver
  driverClass: org.postgresql.Driver

  # the username
  user: pg-user

  # the password
  password: iAMs00perSecrEET

  # the JDBC URL
  url: jdbc:postgresql://db.example.com/db-prod

  # any properties specific to your JDBC driver:
  properties:
    charSet: UTF-8

  # the maximum amount of time to wait on an empty pool before throwing an exception
  maxWaitForConnection: 1s

  # the SQL query to run when validating a connection's liveness
  validationQuery: "/* MyService Health Check */ SELECT 1"

  # the timeout before a connection validation queries fail
  validationQueryTimeout: 3s

  # the minimum number of connections to keep open
  minSize: 8

  # the maximum number of connections to keep open
  maxSize: 32

  # whether or not idle connections should be validated
  checkConnectionWhileIdle: false

  # the amount of time to sleep between runs of the idle connection validation, abandoned cleaner and idle pool resizing
  evictionInterval: 10s

  # the minimum amount of time an connection must sit idle in the pool before it is eligible for eviction
  minIdleTime: 1 minute
```

## Usage

We highly recommend you use JDBI's [SQL Objects API](http://jdbi.org/jdbi2/sql_object_overview/), which allows you to write DAO classes as interfaces:

```java
public interface MyDAO {
  @SqlUpdate("create table something (id int primary key, name varchar(100))")
  void createSomethingTable();

  @SqlUpdate("insert into something (id, name) values (:id, :name)")
  void insert(@Bind("id") int id, @Bind("name") String name);

  @SqlQuery("select name from something where id = :id")
  String findNameById(@Bind("id") int id);
}

final MyDAO dao = database.onDemand(MyDAO.class);
```

This ensures your DAO classes are trivially mockable, as well as encouraging you to extract mapping code (e.g., `ResultSet` -> domain objects) into testable, reusable classes.


## Exception Handling

By adding the `DBIExceptionsBundle` to your application, Dropwizard will automatically unwrap any thrown `SQLException` or `DBIException` instances.
This is critical for debugging, since otherwise only the common wrapper exception's stack trace is logged.


## Prepended Comments

If you're using JDBI's [SQL Objects API](http://jdbi.org/jdbi2/sql_object_overview/) (and you should be),
`dropwizard-jdbi` will automatically prepend the SQL object's class and method name to the SQL query as an SQL comment:

``` sql
/* com.example.service.dao.UserDAO.findByName */
SELECT id, name, email
FROM users
WHERE name = 'Coda';
```

This will allow you to quickly determine the origin of any slow or misbehaving queries.

## Library Support

`dropwizard-jdbi` supports a number of popular libraries data types that
can be automatically serialized into the appropriate SQL type. Here's a
list of what integration `dropwizard-jdbi` provides:

* Guava: support for `Optional<T>` arguments and `ImmutableList<T>` and `ImmutableSet<T>` query results.
* Joda Time: support for `DateTime` arguments and `DateTime` fields in query results
* Java 8: support for `Optional<T>` and kin (`OptionalInt`, etc.) arguments and [java.time](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) arguments.


## Support

Please file bug reports and feature requests in [GitHub issues](https://github.com/dropwizard/dropwizard-jdbi/issues).


## License

Copyright (c) 2012-2019 Dropwizard Team

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.