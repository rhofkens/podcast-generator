### 1.1. **Modular Architecture**
   - Implement a layered architecture (Controller, Service, Repository) to separate concerns.
   - For complex domains, consider Domain-Driven Design (DDD) with modules for domain, infrastructure, and application layers.

### 1.2. **Recommended Package Structure**
   - **controller**: REST Controllers (`@RestController`) handling HTTP requests.
   - **service**: Business logic layer (`@Service`) for processing.
   - **repository**: Data access layer (`@Repository`) for database interactions.
   - **model**: Entity classes, DTOs, and domain objects.
   - **config**: Spring configuration classes (e.g., security, database, etc.).
   - **exception**: Custom exceptions and global error handling.
   - **util**: Utility classes and helpers.
   - **docs**: Directory for Spring REST Docs configurations and output files.

### 1.3. **Microservices & Multi-Module Projects**
   - Structure the project into modules based on bounded contexts for microservices.
   - Use API gateways for routing and aggregation of microservices.
   - Include a dedicated `docs` module for Spring REST Docs configurations.

## 2. **Code Style and Conventions**

### 2.1. **Naming Conventions**
   - Classes: `PascalCase` (e.g., `OrderService`).
   - Methods and variables: `camelCase` (e.g., `findOrderById`).
   - Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_PAGE_SIZE`).
   - Packages: `lowercase` (e.g., `com.example.service`).

### 2.2. **Java Style Rules**
   - **Indentation**: 4 spaces, no tabs.
   - **Braces**: Always use braces for conditionals and loops.
   - **Line Length**: Limit to 120 characters.
   - **Imports**: Avoid wildcard imports, prefer explicit imports.

### 2.3. **Comments and Javadoc**
   - Use Javadoc for public APIs, including Controllers and Service classes.
   - Use method-level comments to explain complex logic.
   - Keep comments up to date and concise.

### 2.4. **Import Organization**
   - Organize imports in this order: standard libraries, third-party libraries, and project-specific.
   - Use static imports sparingly, primarily for test assertions.

## 3. **Dependency Management**

### 3.1. **Maven/Gradle Usage**
   - Use `spring-boot-starter-parent` or `spring-boot-dependencies` for version management.
   - Use dependency management for common dependencies, declared in a parent POM or root Gradle file.

### 3.2. **Versioning and Dependency Management**
   - Use specific versions for dependencies to avoid unexpected behavior.
   - Regularly update dependencies to avoid security vulnerabilities.

### 3.3. **Third-Party Dependencies**
   - Use only well-maintained and widely-used third-party libraries.
   - Ensure dependencies are compatible with each other to avoid conflicts.

## 4. **Application Configuration**

### 4.1. **External Configuration**
   - Use `application.yml` for hierarchical configuration.
   - Keep configuration files organized, with clearly defined sections for database, security, and application settings.

### 4.2. **Environment-Specific Configurations**
   - Use `application-{profile}.yml` for each environment (e.g., `application-dev.yml` for development).
   - Keep environment-specific settings minimal, using profiles for overrides only.

### 4.3. **Sensitive Data Management**
   - Store sensitive data in environment variables or secure vaults.
   - Avoid storing sensitive information in version control.

## 5. **Exception Handling**

### 5.1. **Exception Hierarchy**
   - Create domain-specific exceptions (`OrderNotFoundException`) to handle business errors.
   - Prefer unchecked exceptions (`RuntimeException`) for runtime errors.

### 5.2. **Global Exception Handling**
   - Use `@ControllerAdvice` to implement a global exception handler.
   - Provide structured error responses (`{ "error": "message", "code": 400 }`).

### 5.3. **Logging Exceptions**
   - Log exceptions using SLF4J at an appropriate level.
   - Avoid logging sensitive information.

## 6. **REST API Design**

### 6.1. **Naming Conventions and URL Structure**
   - Use consistent and meaningful resource names (e.g., `/users`, `/orders`).
   - Keep endpoints RESTful and follow naming conventions (`/orders/{id}` for specific resource).

### 6.2. **HTTP Methods**
   - Use HTTP methods appropriately: `GET` for reads, `POST` for creation, `PUT` for updates, `PATCH` for partial updates, `DELETE` for deletions.
   - Use standard HTTP status codes.

### 6.3. **Error Handling and Pagination**
   - Return structured error responses.
   - Implement pagination using parameters like `page`, `size`, and `sort`.

### 6.4. **API Documentation with Spring REST Docs and Slate**
   - Use **Spring REST Docs** for generating API documentation during tests:
     - Annotate test methods with `@Test` and include REST Docs snippets.
     - Place generated documentation snippets in the `docs` folder.
   - Use **Slate** to generate a static HTML API reference:
     - Integrate Spring REST Docs output into a Markdown structure compatible with Slate.
     - Update Slate documentation with every release to ensure it reflects the current API state.

## 7. **Service Layer and Business Logic**

### 7.1. **Service Layer Best Practices**
   - Use `@Service` to encapsulate business logic.
   - Maintain stateless services; use dependency injection for service communication.

### 7.2. **Avoiding Circular Dependencies**
   - Prefer constructor injection to field injection.
   - Split large services into smaller, domain-specific services.

### 7.3. **Cleaner Code with Java 8+ Features**
   - Use `Optional` instead of `null` checks.
   - Leverage streams, lambdas, and method references for concise code.

## 8. **Data Access Layer**

### 8.1. **Database Interaction**
   - Use `@Repository` for Spring Data JPA repositories.
   - Keep database queries optimized; use `JOIN FETCH` where needed for lazy-loaded entities.

### 8.2. **Transaction Management**
   - Annotate service-level methods with `@Transactional`.
   - Avoid placing `@Transactional` at the repository layer.

### 8.3. **Database Migrations**
   - Use Flyway for versioned database migrations.
   - Maintain SQL scripts under a version control directory.

## 9. **Security Best Practices**

### 9.1. **Spring Security Configuration**
   - Use `@EnableWebSecurity` for configuring security settings.
   - Encrypt sensitive information with BCrypt.

### 9.2. **Protecting APIs**
   - Secure REST endpoints using JWT/OAuth2.
   - Use role-based access control with `@PreAuthorize`.

### 9.3. **Vulnerability Protection**
   - Enable CSRF protection for stateful applications.
   - Use validation libraries like Hibernate Validator to sanitize inputs.

## 10. **Performance Optimization**

### 10.1. **Caching**
   - Use `@Cacheable` and Spring Cache for frequently accessed data.
   - Choose a suitable caching backend like Redis.

### 10.2. **Database Optimization**
   - Optimize queries and database schema for performance.
   - Use database indices and limit large result sets.

### 10.3. **Monitoring**
   - Use Actuator for basic application metrics.
   - Integrate Prometheus and Grafana for advanced monitoring.

## 11. **Testing Best Practices**

### 11.1. **Unit & Integration Testing**
   - Use JUnit 5 and Mockito for unit tests.
   - Use `@SpringBootTest` for integration tests involving context loading.

### 11.2. **Test Coverage**
   - Aim for 80%+ test coverage.
   - Focus on critical paths; avoid testing trivial getters/setters.

### 11.3. **Test Documentation with Spring REST Docs**
   - Use REST Docs in integration tests:
     - Annotate API tests with `@AutoConfigureRestDocs`.
     - Use `MockMvc` to capture API interactions.
     - Generate documentation snippets for each endpoint.

## 12. **Code Quality and Maintainability**

### 12.1. **Static Code Analysis**
   - Use SonarQube for code analysis.
   - Integrate static analysis tools in CI pipelines.

### 12.2. **Code Reviews**
   - Implement strict code review practices.
   - Use a checklist for code quality and maintainability.

## 13. **Logging and Monitoring**

### 13.1. **Logging Best Practices**
   - Use SLF4J with Logback for structured logging.
   - Use placeholders instead of string concatenation in log statements.
   - Create extensive logging using INFO, WARN and ERROR log levels.

### 13.2. **Application Monitoring**

## 14. **Deployment and DevOps**

### 14.1. **Containerization**
   - Use Docker for containerizing applications.
   - Keep Docker images small and follow multi-stage builds for optimization

.

### 14.2. **CI/CD Pipelines**
   - Use GitHub Actions, Jenkins, or GitLab CI for CI/CD.
   - Integrate Spring REST Docs generation into CI pipeline.

## 15. **Miscellaneous Best Practices**

### 15.1. **Annotations Usage**
   - Use `@Autowired` with constructor injection.
   - Prefer `@Value` for configuration properties injection.

### 15.2. **Avoiding Anti-Patterns**
   - Avoid static access patterns in the service layer.
   - Keep Lombok usage minimal and controlled.

### 15.3. **Asynchronous Processing**
   - Use `@Async` for asynchronous tasks.
   - Use CompletableFuture for complex asynchronous workflows.

### 15.4. **Lombok Usage**
   - Limit Lombok usage to DTOs and simple beans.
   - Avoid `@Data`; prefer specific annotations like `@Getter`, `@Setter`.

