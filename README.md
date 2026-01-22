# Viewton

Viewton is a Java library for dynamically generating SQL from declarative query input. It focuses on
extensibility, a clean separation of responsibilities, and minimal dependencies in the core.

## Architecture

Viewton is split into multiple Gradle modules:

- **viewton-core**: Core abstractions for parsing, internal query modeling, SQL generation, schema
  representation, and execution contracts.
- **viewton-jooq**: jOOQ-specific execution adapter types (no database connection management).

### Query Flow

1. **Query Input**: External input arrives as either REST-style parameters (`Map<String, String>`) or a
   GraphQL-like declarative query (placeholder abstraction only).
2. **Parsing**: A `QueryInputParser` converts the input into a `QueryModel` (AST/IR).
3. **SQL Generation**: A `SqlGenerator` turns the `QueryModel` and `Schema` into a `SqlStatement`.
4. **Execution**: A `QueryExecutor` runs the SQL using a user-provided execution context.

## Extension Points

Viewton is designed for SPI-style extensibility:

- **Query input formats**: Provide additional `QueryInput` implementations and parsers.
- **Schema providers**: Implement `Schema`, `Table`, `Column`, and `Relationship` adapters.
- **Execution adapters**: Implement `QueryExecutor` for a specific runtime. jOOQ adapters live in
  `viewton-jooq` and require a user-supplied `DSLContext`.
- **Component discovery**: Use `QueryComponentProvider` with `ServiceLoader` to register components.

## Non-goals (Core)

- Database connections, transactions, or pooling.
- Framework-specific configuration (Spring, Hibernate, etc.).
- DSL design for the GraphQL-like query input.
