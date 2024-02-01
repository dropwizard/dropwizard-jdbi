## @JdbiUnitOfWork - Unit of Work Support

Provides a `Unit of Work` annotation for a Jdbi backed Dropwizard service for wrapping resource methods in a transaction
context

- [`Dropwizard`](https://github.com/dropwizard/dropwizard) provides a very
  slick [`@UnitOfWork`](https://www.dropwizard.io/en/latest/manual/hibernate.html) annotation that wraps a transaction
  context around resource methods annotated with this annotation. This is very useful for wrapping multiple calls in a
  single database transaction all of which will succeed or roll back atomically.


- However this support is only available for `Hibernate`. This module provides support for a `Jdbi`backend

## Features

- `transactionality` across multiple datasources when called from a request thread
- `transactionality` across multiple datasources across `multiple threads`
- `excluding` selectively, certain set of URI's from transaction contexts, such as `ELB`, `Health Checks` etc
- `Http GET` methods are excluded from transaction by default.
- `Http POST` methods are wrapped around in a transaction only when annotated with `@JdbiUnitOfWork`

## Usage

- Add the dependency to your `pom.xml`

- Construct a `JdbiUnitOfWorkProvider` from the DBI instance.

  ```java
  JdbiUnitOfWorkProvider provider = JdbiUnitOfWorkProvider.withDefault(dbi); // most common
               or
  JdbiUnitOfWorkProvider provider = JdbiUnitOfWorkProvider.withLinked(dbi); // most common
  ```

  If you are using Guice, you can bind the instance
  ```
  bind(JdbiUnitOfWorkProvider.class).toInstance(provider);
  ```

<br>

- Provide the list of package where the SQL Objects / DAO (to be attached) are located. Classes with Jdbi
  annotations `@SqlQuery` or `@SqlUpdate` or `@SqlBatch` or `@SqlCall` will be picked automatically.

  <br>

  Use `JdbiUnitOfWorkProvider` to generate the proxies. You can also register the classes one by one.

  ```java

  // class level
  SampleDao dao = (SampleDao) provider.getWrappedInstanceForDaoClass(SampleDao.class);
  // use the proxies and pass it as they were normal instances
  resource = new SampleResource(dao);

  // package level
  List<String> daoPackages = Lists.newArrayList("<fq-package-name>", "fq-package-name-2", ...);
  Map<? extends Class, Object> proxies = unitOfWorkProvider.getWrappedInstanceForDaoPackage(daoPackages);
  // use the proxies and pass it as they were normal instances
  resource = ...new SampleResource((SampleDao)proxies.get(SampleDao.class))
  ```

<br>

- Finally, we need to register the event listener with the Jersey Environment using the constructed provider
  ```
  environment.jersey().register(new JdbiUnitOfWorkApplicationEventListener(provider, new HashSet<>()));;
  ```
  In case you'd like to exclude certain URI paths from being monitored, you can pass them into exclude paths;
  ```
  Set<String> excludePaths = new HashSet<>();
  environment.jersey().register(new JdbiUnitOfWorkApplicationEventListener(handleManager, excludePaths));
  ```

<br>

- Start annotating resource methods with `@JdbiUnitOfWork` and you're good to go.
    ```java
    @POST
    @Path("/")
    @JdbiUnitOfWork
    public RequestResponse createRequest() {
          ..do stateful work (across multiple Dao's)
          return response
    }
    ```