# US-001 - Create Habit

## Related User Story

[US-001 - Создание привычки](../user-stories/US-001-create-habit.md)

## Endpoint

- **Method:** `POST`
- **Path:** `/api/v1/habits`

## Request

### Headers

| Header         | Value              | Required |
|----------------|--------------------|----------|
| `Content-Type` | `application/json` | yes      |

### Body

```json
{
  "name": "string",
  "description": "string"
}
```

### Request Fields

| Field         | Type     | Required | Validation                                                     |
|---------------|----------|----------|----------------------------------------------------------------|
| `name`        | `string` | yes      | После удаления пробелов в начале и конце не должно быть пустым |
| `description` | `string` | no       |                                                                |

## Successful Response

### Status

`201 Created`

### Body

```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "active": true
}
```

### Response Fields

| Field         | Type             | Description                                                      |
|---------------|------------------|------------------------------------------------------------------|
| `id`          | `integer`        | ID созданной привычки                                            |
| `name`        | `string`         | Название созданной привычки                                      |
| `description` | `string \| null` | Описание созданной привычки или `null`, если описание не указано |
| `active`      | `boolean`        | Активна ли созданная привычка                                    |

## Error Responses

| Situation                                                               | Status            |
|-------------------------------------------------------------------------|-------------------|
| `Название отсутствует, имеет значение null или является пустой строкой` | `400 Bad Request` |
| `Название состоит только из пробельных символов`                        | `400 Bad Request` |
| `Название уже существует`                                               | `409 Conflict`    |

### Error Body

```json
{
  "status": 400,
  "message": "string",
  "errors": [
    {
      "field": "string",
      "message": "string"
    }
  ]
}
```

### Error Fields

| Field            | Type      | Description                                 |
|------------------|-----------|---------------------------------------------|
| `status`         | `integer` | HTTP статус ошибки                          |
| `message`        | `string`  | Короткое сообщение об ошибке                |
| `errors`         | `array`   | Массив с деталями ошибки, может быть пустым |
| `errors.field`   | `string`  | Поле, в котором ошибка                      |
| `errors.message` | `string`  | Описание ошибки в поле                      |

## Business Rules

- Название привычки обязательно
- Названия не чувствительны к регистру. При получении названия API удаляет все пробельные символы в начале и конце строки
- Описание привычки необязательно
- Нельзя создать две привычки с одинаковым названием (даже если привычка архивирована)
- При создании backend автоматически назначает привычке статус «активна»
