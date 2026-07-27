# Habit Tracker API

## Base path

`/api/v1`

## OpenAPI

Для запущенного приложения доступны:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

При добавлении или изменении API endpoint в рамках User Story необходимо в том же Pull Request:

- обновить OpenAPI-аннотации;
- обновить соответствующий документ в `docs/api`, если изменился контракт;
- проверить Swagger UI и `/v3/api-docs` вручную.

## Endpoints

| User Story | Method | Endpoint                   | Status      |
|------------|--------|----------------------------|-------------|
| US-001     | POST   | `/habits`                  | Implemented |
| US-003     | GET    | `/habits`                  | Implemented |
| US-002     | GET    | `/habits/{id}`             | Implemented |
| US-006     | POST   | `/habits/{id}/completions` | Implemented |
| US-007     | DELETE | `/habits/{id}/completions` | Implemented |
| US-004     | PUT    | `/habits/{id}`             | Planned     |
| US-005     | DELETE | `/habits/{id}`             | Planned     |
