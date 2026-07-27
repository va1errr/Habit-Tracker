# US-007 - Cancel Habit Completion

## Related User Story

[US-007 - Отмена выполнения привычки](../user-stories/US-007-cancel-completion.md)

## Endpoint

- **Method:** `DELETE`
- **Path:** `/api/v1/habits/{id}/completions`

## Request

### Path Parameters

- `id` - ID привычки (`integer (int64)`)

## Successful Response

### Status

`204 No Content`

### Body

Отсутствует

## Error Responses

| Situation                                                            | Status            |
|----------------------------------------------------------------------|-------------------|
| `Привычка с данным id не найдена`                                    | `404 Not Found`   |
| `Привычка с данным id архивирована`                                  | `404 Not Found`   |
| `Привычка ещё не выполнена за текущий день`                          | `409 Conflict`    |
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

- Привычка считается выполненной с момента выполнения до конца текущего дня
- Часовой пояс используется Europe/Moscow
- Нельзя отменить выполнение архивированной привычки
- Удаляется только факт выполнения за текущий день; выполнения за предыдущие даты сохраняются.
