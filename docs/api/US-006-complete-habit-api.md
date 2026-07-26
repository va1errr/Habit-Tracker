# US-006 - Complete Habit

## Related User Story

[US-006 - Выполнение привычки](../user-stories/US-006-complete-habit.md)

## Endpoint

- **Method:** `POST`
- **Path:** `/api/v1/habits/{id}/completions`

## Request

### Path Parameters

- `id` - ID привычки (`integer (int64)`)

## Successful Response

### Status

`201 Created`

### Body

```json
{
  "id": 1,
  "habitId": 1,
  "completionDate": "2026-07-27"
}
```

### Response Fields

| Field            | Type      | Description                      |
|------------------|-----------|----------------------------------|
| `id`             | `integer` | ID отметки о выполнении привычки |
| `habitId`        | `integer` | ID привычки                      |
| `completionDate` | `string`  | Дата выполнения                  |

## Error Responses

| Situation                                                            | Status            |
|----------------------------------------------------------------------|-------------------|
| `Привычка с данным id не найдена`                                    | `404 Not Found`   |
| `Привычка с данным id архивирована`                                  | `404 Not Found`   |
| `Привычка уже выполнена за текущий день`                             | `409 Conflict`    |
| `Некорректный id (невозможно преобразовать в 64-битное целое число)` | `400 Bad Request` |

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

- Для каждого дня хранится отдельный факт выполнения привычки
- Выполнение можно отметить только один раз за день
- Привычка считается выполненной с момента выполнения до конца текущего дня
- Часовой пояс используется Europe/Moscow
- Архивированная привычка не видна в общем списке, ее нельзя выполнить
