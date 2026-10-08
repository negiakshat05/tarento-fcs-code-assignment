# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```
Yes, I would consider refactoring the database access layer to make the approach more consistent. Different parts of the application currently use different strategies, so I would prefer keeping database-specific logic in a repository/DAO layer and business logic in the use cases or services. This would make the code easier to maintain and test. I would do this gradually, though, focusing first on areas where the current approach is causing duplication or making maintenance difficult.

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```
I think both approaches have their advantages. OpenAPI gives us a clear contract, generates some boilerplate, and helps keep the API and implementation in sync, which is useful for larger or public APIs. Manually creating endpoints is simpler and gives more flexibility for small internal APIs, but there is more chance of the documentation and implementation getting out of sync. For this project, I would prefer OpenAPI for larger or public APIs and manual implementation for simpler internal ones.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```
I'd prioritize tests based on business risk rather than simply trying to maximize coverage. I would start with unit tests for the core business rules and validations, followed by integration tests for the important API and database flows. I would also make sure important failure cases are covered, such as invalid locations, duplicate BU codes and invalid warehouse replacements. Over time, I would maintain a reasonable coverage level and make sure new business logic comes with tests, while treating coverage as an indicator rather than the only measure of test quality.
```