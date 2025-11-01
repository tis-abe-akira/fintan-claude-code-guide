package com.example.taskapi.controller;

import com.example.taskapi.entity.Task;
import com.example.taskapi.exception.ResourceNotFoundException;
import com.example.taskapi.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController テスト")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private Task sampleTask;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        sampleTask = new Task(1L, "Sample Task", "Sample Description", false, now, now);
    }

    // ========== GET /api/tasks - 全タスク取得 ==========

    @Test
    @DisplayName("全タスク取得_正常系_タスクが存在する場合200OKとタスクリストを返す")
    void getAllTasks_正常系_タスクが存在する() throws Exception {
        // Arrange
        Task task1 = new Task(1L, "Task 1", "Description 1", false, now, now);
        Task task2 = new Task(2L, "Task 2", "Description 2", true, now, now);
        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].completed").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].completed").value(true));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @DisplayName("全タスク取得_正常系_タスクが存在しない場合200OKと空リストを返す")
    void getAllTasks_正常系_タスクが存在しない() throws Exception {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(taskService, times(1)).getAllTasks();
    }

    // ========== GET /api/tasks/{id} - 特定タスク取得 ==========

    @Test
    @DisplayName("特定タスク取得_正常系_タスクが存在する場合200OKとタスクを返す")
    void getTaskById_正常系_タスクが存在する() throws Exception {
        // Arrange
        when(taskService.getTaskById(1L)).thenReturn(sampleTask);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.description").value("Sample Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    @DisplayName("特定タスク取得_異常系_タスクが存在しない場合404NotFoundを返す")
    void getTaskById_異常系_タスクが存在しない() throws Exception {
        // Arrange
        when(taskService.getTaskById(999L)).thenThrow(new ResourceNotFoundException("Task", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));

        verify(taskService, times(1)).getTaskById(999L);
    }

    // ========== POST /api/tasks - タスク作成 ==========

    @Test
    @DisplayName("タスク作成_正常系_有効なタスクで201Createdとタスクを返す")
    void createTask_正常系_有効なタスク() throws Exception {
        // Arrange
        Task newTask = new Task(null, "New Task", "New Description", false, null, null);
        Task createdTask = new Task(1L, "New Task", "New Description", false, now, now);
        when(taskService.createTask(any(Task.class))).thenReturn(createdTask);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("タスク作成_異常系_タイトルが空の場合400BadRequestを返す")
    void createTask_異常系_タイトルが空() throws Exception {
        // Arrange
        Task invalidTask = new Task(null, "", "Description", false, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(taskService, times(0)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("タスク作成_異常系_タイトルがnullの場合400BadRequestを返す")
    void createTask_異常系_タイトルがnull() throws Exception {
        // Arrange
        Task invalidTask = new Task(null, null, "Description", false, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verify(taskService, times(0)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("タスク作成_異常系_タイトルが100文字を超える場合400BadRequestを返す")
    void createTask_異常系_タイトルが長すぎる() throws Exception {
        // Arrange
        String longTitle = "a".repeat(101);
        Task invalidTask = new Task(null, longTitle, "Description", false, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verify(taskService, times(0)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("タスク作成_異常系_説明が500文字を超える場合400BadRequestを返す")
    void createTask_異常系_説明が長すぎる() throws Exception {
        // Arrange
        String longDescription = "a".repeat(501);
        Task invalidTask = new Task(null, "Valid Title", longDescription, false, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verify(taskService, times(0)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("タスク作成_異常系_completedがnullの場合400BadRequestを返す")
    void createTask_異常系_completedがnull() throws Exception {
        // Arrange
        Task invalidTask = new Task(null, "Valid Title", "Description", null, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verify(taskService, times(0)).createTask(any(Task.class));
    }

    // ========== PUT /api/tasks/{id} - タスク更新 ==========

    @Test
    @DisplayName("タスク更新_正常系_有効なタスクで200OKと更新されたタスクを返す")
    void updateTask_正常系_有効なタスク() throws Exception {
        // Arrange
        Task updateTask = new Task(null, "Updated Task", "Updated Description", true, null, null);
        Task updatedTask = new Task(1L, "Updated Task", "Updated Description", true, now, now);
        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTask);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    @DisplayName("タスク更新_異常系_タスクが存在しない場合404NotFoundを返す")
    void updateTask_異常系_タスクが存在しない() throws Exception {
        // Arrange
        Task updateTask = new Task(null, "Updated Task", "Updated Description", true, null, null);
        when(taskService.updateTask(eq(999L), any(Task.class)))
                .thenThrow(new ResourceNotFoundException("Task", 999L));

        // Act & Assert
        mockMvc.perform(put("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));

        verify(taskService, times(1)).updateTask(eq(999L), any(Task.class));
    }

    @Test
    @DisplayName("タスク更新_異常系_バリデーションエラーの場合400BadRequestを返す")
    void updateTask_異常系_バリデーションエラー() throws Exception {
        // Arrange
        Task invalidTask = new Task(null, "", "Description", false, null, null);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verify(taskService, times(0)).updateTask(eq(1L), any(Task.class));
    }

    // ========== DELETE /api/tasks/{id} - タスク削除 ==========

    @Test
    @DisplayName("タスク削除_正常系_タスクが存在する場合204NoContentを返す")
    void deleteTask_正常系_タスクが存在する() throws Exception {
        // Arrange
        doNothing().when(taskService).deleteTask(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    @DisplayName("タスク削除_異常系_タスクが存在しない場合404NotFoundを返す")
    void deleteTask_異常系_タスクが存在しない() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Task", 999L)).when(taskService).deleteTask(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));

        verify(taskService, times(1)).deleteTask(999L);
    }

    // ========== PATCH /api/tasks/{id}/toggle - 完了状態切り替え ==========

    @Test
    @DisplayName("完了状態切り替え_正常系_タスクが存在する場合200OKと更新されたタスクを返す")
    void toggleTaskCompletion_正常系_タスクが存在する() throws Exception {
        // Arrange
        Task toggledTask = new Task(1L, "Sample Task", "Sample Description", true, now, now);
        when(taskService.toggleTaskCompletion(1L)).thenReturn(toggledTask);

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/1/toggle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService, times(1)).toggleTaskCompletion(1L);
    }

    @Test
    @DisplayName("完了状態切り替え_異常系_タスクが存在しない場合404NotFoundを返す")
    void toggleTaskCompletion_異常系_タスクが存在しない() throws Exception {
        // Arrange
        when(taskService.toggleTaskCompletion(999L)).thenThrow(new ResourceNotFoundException("Task", 999L));

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/999/toggle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));

        verify(taskService, times(1)).toggleTaskCompletion(999L);
    }
}
