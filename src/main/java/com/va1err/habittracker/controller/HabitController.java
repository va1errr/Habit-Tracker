package com.va1err.habittracker.controller;

import com.va1err.habittracker.dto.*;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Habits",
        description = "Управление привычками и отметками об их выполнении"
)
@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @Operation(
            summary = "Создать привычку",
            description = "Создаёт новую активную привычку"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Привычка успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HabitResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "validationError",
                                    summary = "Ошибка валидации",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Validation failed",
                                              "errors": [
                                                {
                                                  "field": "name",
                                                  "message": "must not be blank"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Привычка с таким названием уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "duplicateHabitName",
                                    summary = "Название привычки уже занято",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Habit name already exists!",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные новой привычки",
                    required = true
            )
            @Valid @RequestBody
            CreateHabitRequest request
    ) {
        Habit habit = habitService.createHabit(request.name(), request.description());

        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.isActive()
        );
    }

    @Operation(
            summary = "Получить список привычек",
            description = "Возвращает список активных привычек с признаком выполнения за текущий день"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список активных привычек",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = HabitListItemResponse.class)
                    )
            )
    )
    @GetMapping
    public List<HabitListItemResponse> listHabits() {
        return habitService.listHabits();
    }

    @Operation(
            summary = "Получить привычку",
            description = "Возвращает активную привычку по её ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные привычки",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HabitDetailsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "invalidHabitId",
                                    summary = "Некорректный ID",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid request parameter",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Активная привычка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "habitNotFound",
                                    summary = "Привычка не найдена",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Habit not found",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public HabitDetailsResponse getById(
            @Parameter(
                    description = "ID привычки",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return habitService.getById(id);
    }

    @Operation(
            summary = "Отметить привычку выполненной",
            description = "Создаёт отметку о выполнении активной привычки за текущий день"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Выполнение привычки отмечено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HabitCompletionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "invalidHabitId",
                                    summary = "Некорректный ID",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid request parameter",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Активная привычка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "habitNotFound",
                                    summary = "Привычка не найдена",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Habit not found",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Привычка уже выполнена за текущий день",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "habitAlreadyCompleted",
                                    summary = "Привычка уже выполнена сегодня",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Habit is already completed today",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{id}/completions")
    @ResponseStatus(HttpStatus.CREATED)
    public HabitCompletionResponse completeHabit(
            @Parameter(
                    description = "ID привычки",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return habitService.completeHabit(id);
    }

    @Operation(
            summary = "Отменить выполнение привычки",
            description = "Удаляет отметку о выполнении активной привычки за текущий день"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Выполнение привычки отменено",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "invalidHabitId",
                                    summary = "Некорректный ID",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid request parameter",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Активная привычка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "habitNotFound",
                                    summary = "Привычка не найдена",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Habit not found",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Привычка не выполнена за текущий день",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "habitNotCompleted",
                                    summary = "Привычка не выполнена сегодня",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Habit is not completed today",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}/completions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelHabitCompletion(
            @Parameter(
                    description = "ID привычки",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        habitService.cancelHabitCompletion(id);
    }

    @Operation(
            summary = "Изменить привычку",
            description = "Изменяет существующую привычку"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Привычка успешно изменена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HabitResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса или формат ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "invalidHabitId",
                                            summary = "Некорректный ID",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "message": "Invalid request parameter",
                                                      "errors": []
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "validationError",
                                            summary = "Ошибка валидации",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "message": "Validation failed",
                                                      "errors": [
                                                        {
                                                          "field": "name",
                                                          "message": "must not be blank"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Активная привычка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "habitNotFound",
                                    summary = "Привычка не найдена",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Habit not found",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Привычка с таким названием уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "duplicateHabitName",
                                    summary = "Название привычки уже занято",
                                    value = """
                                            {
                                              "status": 409,
                                              "message": "Habit name already exists!",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{id}")
    public HabitResponse updateHabit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для изменения привычки",
                    required = true
            )
            @Valid @RequestBody UpdateHabitRequest request,
            @Parameter(
                    description = "ID изменяемой привычки",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        Habit habit = habitService.updateHabit(
                id,
                request.getName(),
                request.getDescription(),
                request.descriptionPresent()
        );

        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.isActive()
        );
    }

    @Operation(
            summary = "Архивировать привычку",
            description = "Архивирует активную привычку без удаления истории её выполнений"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Привычка успешно архивирована",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "invalidHabitId",
                                    summary = "Некорректный ID",
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Invalid request parameter",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Активная привычка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "habitNotFound",
                                    summary = "Привычка не найдена",
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Habit not found",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveHabit(
            @Parameter(
                    description = "ID архивируемой привычки",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        habitService.archiveHabit(id);
    }

}
