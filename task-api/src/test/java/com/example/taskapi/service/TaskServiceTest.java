package com.example.taskapi.service;

import com.example.taskapi.entity.Task;
import com.example.taskapi.exception.ResourceNotFoundException;
import com.example.taskapi.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task validTask;
    private Task anotherTask;

    @BeforeEach
    void setUp() {
        validTask = new Task();
        validTask.setId(1L);
        validTask.setTitle("Test Task");
        validTask.setDescription("Test Description");
        validTask.setCompleted(false);
        validTask.setCreatedAt(LocalDateTime.now());
        validTask.setUpdatedAt(LocalDateTime.now());

        anotherTask = new Task();
        anotherTask.setId(2L);
        anotherTask.setTitle("Another Task");
        anotherTask.setDescription("Another Description");
        anotherTask.setCompleted(true);
        anotherTask.setCreatedAt(LocalDateTime.now());
        anotherTask.setUpdatedAt(LocalDateTime.now());
    }

    // ========================================
    // 全タスク取得のテスト
    // ========================================

    @Test
    @DisplayName("全タスクを取得できる")
    void testGetAllTasks() {
        // Given
        List<Task> tasks = Arrays.asList(validTask, anotherTask);
        when(taskMapper.findAll()).thenReturn(tasks);

        // When
        List<Task> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(validTask, anotherTask);
        verify(taskMapper, times(1)).findAll();
    }

    @Test
    @DisplayName("タスクが存在しない場合、空のリストを返す")
    void testGetAllTasks_Empty() {
        // Given
        when(taskMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Task> result = taskService.getAllTasks();

        // Then
        assertThat(result).isEmpty();
        verify(taskMapper, times(1)).findAll();
    }

    // ========================================
    // 特定タスク取得のテスト
    // ========================================

    @Test
    @DisplayName("IDでタスクを取得できる")
    void testGetTaskById() {
        // Given
        when(taskMapper.findById(1L)).thenReturn(validTask);

        // When
        Task result = taskService.getTaskById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskMapper, times(1)).findById(1L);
    }

    @Test
    @DisplayName("存在しないIDでタスク取得すると例外をスローする")
    void testGetTaskById_NotFound() {
        // Given
        when(taskMapper.findById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskMapper, times(1)).findById(999L);
    }

    // ========================================
    // タスク作成のテスト
    // ========================================

    @Test
    @DisplayName("タスクを作成できる")
    void testCreateTask() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setCompleted(true);

        // When
        Task result = taskService.createTask(newTask);

        // Then
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getCompleted()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(taskMapper, times(1)).insert(newTask);
    }

    @Test
    @DisplayName("completedがnullの場合、falseに設定される")
    void testCreateTask_CompletedNull() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setCompleted(null);

        // When
        Task result = taskService.createTask(newTask);

        // Then
        assertThat(result.getCompleted()).isFalse();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(taskMapper, times(1)).insert(newTask);
    }

    @Test
    @DisplayName("作成日時と更新日時が自動設定される")
    void testCreateTask_TimestampsSet() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setCompleted(false);

        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

        // When
        Task result = taskService.createTask(newTask);

        // Then
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);
        assertThat(result.getCreatedAt()).isAfter(beforeCreate);
        assertThat(result.getCreatedAt()).isBefore(afterCreate);
        assertThat(result.getUpdatedAt()).isAfter(beforeCreate);
        assertThat(result.getUpdatedAt()).isBefore(afterCreate);
        verify(taskMapper, times(1)).insert(newTask);
    }

    @Test
    @DisplayName("タイトルが100文字のタスクを作成できる")
    void testCreateTask_TitleMaxLength() {
        // Given
        String maxLengthTitle = "a".repeat(100);
        Task newTask = new Task();
        newTask.setTitle(maxLengthTitle);
        newTask.setDescription("Description");
        newTask.setCompleted(false);

        // When
        Task result = taskService.createTask(newTask);

        // Then
        assertThat(result.getTitle()).hasSize(100);
        verify(taskMapper, times(1)).insert(newTask);
    }

    @Test
    @DisplayName("説明が500文字のタスクを作成できる")
    void testCreateTask_DescriptionMaxLength() {
        // Given
        String maxLengthDescription = "a".repeat(500);
        Task newTask = new Task();
        newTask.setTitle("Title");
        newTask.setDescription(maxLengthDescription);
        newTask.setCompleted(false);

        // When
        Task result = taskService.createTask(newTask);

        // Then
        assertThat(result.getDescription()).hasSize(500);
        verify(taskMapper, times(1)).insert(newTask);
    }

    @Test
    @DisplayName("説明がnullのタスクを作成できる")
    void testCreateTask_DescriptionNull() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("Title");
        newTask.setDescription(null);
        newTask.setCompleted(false);

        // When
        Task result = taskService.createTask(newTask);

        // Then
        assertThat(result.getDescription()).isNull();
        verify(taskMapper, times(1)).insert(newTask);
    }

    // ========================================
    // タスク更新のテスト
    // ========================================

    @Test
    @DisplayName("タスクを更新できる")
    void testUpdateTask() {
        // Given
        Task updateDetails = new Task();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        when(taskMapper.findById(1L)).thenReturn(validTask);

        // When
        Task result = taskService.updateTask(1L, updateDetails);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getCompleted()).isTrue();
        assertThat(result.getUpdatedAt()).isNotNull();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper, times(1)).update(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("存在しないタスクを更新すると例外をスローする")
    void testUpdateTask_NotFound() {
        // Given
        Task updateDetails = new Task();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        when(taskMapper.findById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(999L, updateDetails))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskMapper, times(1)).findById(999L);
        verify(taskMapper, never()).update(any(Task.class));
    }

    @Test
    @DisplayName("更新時に更新日時が自動更新される")
    void testUpdateTask_UpdatedAtRefreshed() {
        // Given
        Task updateDetails = new Task();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        LocalDateTime originalUpdatedAt = validTask.getUpdatedAt();
        when(taskMapper.findById(1L)).thenReturn(validTask);

        // When
        Task result = taskService.updateTask(1L, updateDetails);

        // Then
        assertThat(result.getUpdatedAt()).isAfter(originalUpdatedAt);
        verify(taskMapper, times(1)).update(any(Task.class));
    }

    // ========================================
    // タスク削除のテスト
    // ========================================

    @Test
    @DisplayName("タスクを削除できる")
    void testDeleteTask() {
        // Given
        when(taskMapper.findById(1L)).thenReturn(validTask);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskMapper, times(1)).findById(1L);
        verify(taskMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("存在しないタスクを削除すると例外をスローする")
    void testDeleteTask_NotFound() {
        // Given
        when(taskMapper.findById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskMapper, times(1)).findById(999L);
        verify(taskMapper, never()).deleteById(anyLong());
    }

    // ========================================
    // 完了状態の切り替えのテスト
    // ========================================

    @Test
    @DisplayName("完了状態をfalseからtrueに切り替えられる")
    void testToggleTaskCompletion_FalseToTrue() {
        // Given
        validTask.setCompleted(false);
        when(taskMapper.findById(1L)).thenReturn(validTask);

        // When
        Task result = taskService.toggleTaskCompletion(1L);

        // Then
        assertThat(result.getCompleted()).isTrue();
        verify(taskMapper, times(1)).update(validTask);
    }

    @Test
    @DisplayName("完了状態をtrueからfalseに切り替えられる")
    void testToggleTaskCompletion_TrueToFalse() {
        // Given
        validTask.setCompleted(true);
        when(taskMapper.findById(1L)).thenReturn(validTask);

        // When
        Task result = taskService.toggleTaskCompletion(1L);

        // Then
        assertThat(result.getCompleted()).isFalse();
        verify(taskMapper, times(1)).update(validTask);
    }

    @Test
    @DisplayName("存在しないタスクの完了状態を切り替えると例外をスローする")
    void testToggleTaskCompletion_NotFound() {
        // Given
        when(taskMapper.findById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> taskService.toggleTaskCompletion(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskMapper, times(1)).findById(999L);
        verify(taskMapper, never()).update(any(Task.class));
    }

    // ========================================
    // 検索機能のテスト
    // ========================================

    @Test
    @DisplayName("完了状態で検索できる - completed=true")
    void testGetTasksByCompleted_True() {
        // Given
        List<Task> completedTasks = Arrays.asList(anotherTask);
        when(taskMapper.findByCompleted(true)).thenReturn(completedTasks);

        // When
        List<Task> result = taskService.getTasksByCompleted(true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompleted()).isTrue();
        verify(taskMapper, times(1)).findByCompleted(true);
    }

    @Test
    @DisplayName("完了状態で検索できる - completed=false")
    void testGetTasksByCompleted_False() {
        // Given
        List<Task> incompleteTasks = Arrays.asList(validTask);
        when(taskMapper.findByCompleted(false)).thenReturn(incompleteTasks);

        // When
        List<Task> result = taskService.getTasksByCompleted(false);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompleted()).isFalse();
        verify(taskMapper, times(1)).findByCompleted(false);
    }

    @Test
    @DisplayName("タイトルで部分一致検索できる")
    void testGetTasksByTitleContaining() {
        // Given
        List<Task> tasks = Arrays.asList(validTask);
        when(taskMapper.findByTitleContaining("Test")).thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasksByTitleContaining("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Test");
        verify(taskMapper, times(1)).findByTitleContaining("Test");
    }

    @Test
    @DisplayName("検索キーワードがnullの場合、全タスクを返す")
    void testGetTasksByTitleContaining_Null() {
        // Given
        List<Task> allTasks = Arrays.asList(validTask, anotherTask);
        when(taskMapper.findAll()).thenReturn(allTasks);

        // When
        List<Task> result = taskService.getTasksByTitleContaining(null);

        // Then
        assertThat(result).hasSize(2);
        verify(taskMapper, times(1)).findAll();
        verify(taskMapper, never()).findByTitleContaining(any());
    }

    @Test
    @DisplayName("検索キーワードが空文字の場合、全タスクを返す")
    void testGetTasksByTitleContaining_Empty() {
        // Given
        List<Task> allTasks = Arrays.asList(validTask, anotherTask);
        when(taskMapper.findAll()).thenReturn(allTasks);

        // When
        List<Task> result = taskService.getTasksByTitleContaining("   ");

        // Then
        assertThat(result).hasSize(2);
        verify(taskMapper, times(1)).findAll();
        verify(taskMapper, never()).findByTitleContaining(any());
    }

    @Test
    @DisplayName("作成日時で降順に並び替えて取得できる")
    void testGetAllTasksOrderByCreatedAtDesc() {
        // Given
        List<Task> tasks = Arrays.asList(anotherTask, validTask);
        when(taskMapper.findAllOrderByCreatedAtDesc()).thenReturn(tasks);

        // When
        List<Task> result = taskService.getAllTasksOrderByCreatedAtDesc();

        // Then
        assertThat(result).hasSize(2);
        verify(taskMapper, times(1)).findAllOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("作成日時で昇順に並び替えて取得できる")
    void testGetAllTasksOrderByCreatedAtAsc() {
        // Given
        List<Task> tasks = Arrays.asList(validTask, anotherTask);
        when(taskMapper.findAllOrderByCreatedAtAsc()).thenReturn(tasks);

        // When
        List<Task> result = taskService.getAllTasksOrderByCreatedAtAsc();

        // Then
        assertThat(result).hasSize(2);
        verify(taskMapper, times(1)).findAllOrderByCreatedAtAsc();
    }
}
