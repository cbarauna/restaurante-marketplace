# Restaurante Marketplace — Backend

Backend do Restaurante Marketplace, construído com Java 21 e Quarkus 3.39.5.

## Endpoints locais

- GraphQL: <http://localhost:8080/graphql>
- GraphQL UI: <http://localhost:8080/q/graphql-ui/>
- Dev UI: <http://localhost:8080/q/dev/>
- Health: <http://localhost:9000/q/health>

Em desenvolvimento, o Quarkus Dev Services inicia PostgreSQL e Keycloak automaticamente quando o Docker está disponível. O realm `restaurante-marketplace` é importado com duas contas exclusivamente locais:

| Usuário | Senha | Papel |
|---|---|---|
| `owner@restaurante.local` | `owner-dev-password` | `restaurant-owner` |
| `admin@restaurante.local` | `admin-dev-password` | `platform-admin` |
| `manager@restaurante.local` | `manager-dev-password` | Papel local apó aceitar convite |
| `operator@restaurante.local` | `operator-dev-password` | Papel local apó aceitar convite |

Essas credenciais são públicas e não devem ser reutilizadas fora do ambiente de desenvolvimento. Os testes automatizados usam identidades simuladas e não iniciam o Keycloak.

Para obter um token local, abra o card OpenID Connect na Dev UI, clique em **Keycloak provider**, informe uma das contas acima e use a ação de teste. O perfil de desenvolvimento configura explicitamente o grant `password` para exibir esse formulário. O backend espera o cabeçalho `Authorization: Bearer <token>` nas operações protegidas.

Gerentes e operadores não recebem papéis globais no Keycloak. A permissão é concedida no banco da aplicação quando o convite do restaurante é aceito.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Executando os testes

```shell script
./mvnw test
```

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/backend-0.1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplified JPA/Hibernate data access layer with active record and repository patterns
- Micrometer OpenTelemetry Bridge ([guide](https://quarkus.io/guides/telemetry-micrometer-to-opentelemetry)): Micrometer registry implemented by the OpenTelemetry SDK
- Scheduler ([guide](https://quarkus.io/guides/scheduler)): Schedule recurring tasks and periodic jobs using cron expressions or fixed intervals
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Bean validation using Hibernate Validator and Jakarta Validation annotations
- Flyway ([guide](https://quarkus.io/guides/flyway)): Handle your database schema migrations
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Generate OpenAPI schemas and serve Swagger UI for REST API documentation
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
- OpenID Connect ([guide](https://quarkus.io/guides/security-openid-connect)): Secure applications with OpenID Connect and OAuth 2.0 using bearer tokens and authorization code flow
- SmallRye GraphQL ([guide](https://quarkus.io/guides/smallrye-graphql)): Create GraphQL Endpoints using the code-first approach from MicroProfile GraphQL
