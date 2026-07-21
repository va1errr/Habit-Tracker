# US-002 - Get Habit

## Related User Story

[US-002 - Просмотр привычки](../user-stories/US-002-get-habit.md)

## Endpoint

- **Method:** `GET`
- **Path:** `/api/v1/habits/{id}`

## Path Parameters

- `id` - ID запрашиваемой привычки (`integer (int64)`)

## Successful Response

### Status

`200 OK`

### Body

```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "completedToday": false
}
```

### Response Fields

| Field            | Type             | Description                                            |
|------------------|------------------|--------------------------------------------------------|
| `id`             | `integer`        | ID привычки                                            |
| `name`           | `string`         | Название привычки                                      |
| `description`    | `string \| null` | Описание привычки или `null`, если описание не указано |
| `completedToday` | `boolean`        | Выполнена ли привычка за текущий день                  |

## Error Responses

| Situation                                                            | Status            |
|----------------------------------------------------------------------|-------------------|
| `Привычка с данным id не найдена`                                    | `404 Not Found`   |
| `Привычка с данным id архивирована`                                  | `404 Not Found`   |
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
- Привычка считается выполненной с момента выполнения до конца текущего дня
- Часовой пояс используется `Europe/Moscow`
- Endpoint возвращает данные только активной привычки
- `completedToday` имеет значение `true`, если для привычки существует факт выполнения за текущую дату в `Europe/Moscow`;
  иначе `false`.
