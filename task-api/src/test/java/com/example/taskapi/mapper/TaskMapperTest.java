package com.example.taskapi.mapper;

import com.example.taskapi.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.flyway.enabled=false"
})
@Sql(scripts = "/db/migration/V1__Create_tasks_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(statements = "DELETE FROM tasks", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    private Task createValidTask(String title, String description, Boolean completed) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    @BeforeEach
    void setUp() {
        // テストごとにテーブルをクリーンな状態にする
    }

    // ========================================
    // 基本的なCRUDテスト
    // ========================================

    @Test
    @DisplayName("タスクを作成できる")
    void testInsert() {
        // Given
        Task task = createValidTask("Test Task", "Test Description", false);

        // When
        taskMapper.insert(task);

        // Then
        assertThat(task.getId()).isNotNull();
        assertThat(task.getId()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("作成したタスクをIDで取得できる")
    void testFindById() {
        // Given
        Task task = createValidTask("Test Task", "Test Description", false);
        taskMapper.insert(task);

        // When
        Task foundTask = taskMapper.findById(task.getId());

        // Then
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getId()).isEqualTo(task.getId());
        assertThat(foundTask.getTitle()).isEqualTo("Test Task");
        assertThat(foundTask.getDescription()).isEqualTo("Test Description");
        assertThat(foundTask.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("存在しないIDで取得するとnullが返る")
    void testFindById_NotFound() {
        // When
        Task foundTask = taskMapper.findById(999L);

        // Then
        assertThat(foundTask).isNull();
    }

    @Test
    @DisplayName("全てのタスクを取得できる")
    void testFindAll() {
        // Given
        Task task1 = createValidTask("Task 1", "Description 1", false);
        Task task2 = createValidTask("Task 2", "Description 2", true);
        Task task3 = createValidTask("Task 3", "Description 3", false);
        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        // When
        List<Task> tasks = taskMapper.findAll();

        // Then
        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 3");
    }

    @Test
    @DisplayName("タスクが存在しない場合、findAllは空のリストを返す")
    void testFindAll_Empty() {
        // When
        List<Task> tasks = taskMapper.findAll();

        // Then
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("タスクを更新できる")
    void testUpdate() {
        // Given
        Task task = createValidTask("Original Title", "Original Description", false);
        taskMapper.insert(task);

        // When
        task.setTitle("Updated Title");
        task.setDescription("Updated Description");
        task.setCompleted(true);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);

        // Then
        Task updatedTask = taskMapper.findById(task.getId());
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedTask.getCompleted()).isTrue();
    }

    @Test
    @DisplayName("タスクを削除できる")
    void testDeleteById() {
        // Given
        Task task = createValidTask("Task to Delete", "Description", false);
        taskMapper.insert(task);
        Long taskId = task.getId();

        // When
        taskMapper.deleteById(taskId);

        // Then
        Task deletedTask = taskMapper.findById(taskId);
        assertThat(deletedTask).isNull();
    }

    @Test
    @DisplayName("存在しないIDで削除しても例外が発生しない")
    void testDeleteById_NotFound() {
        // When & Then (例外が発生しないことを確認)
        taskMapper.deleteById(999L);
    }

    // ========================================
    // バリデーション関連のテスト
    // ========================================

    @Test
    @DisplayName("タイトルが100文字の場合、正常に作成できる")
    void testInsert_TitleMaxLength() {
        // Given
        String maxLengthTitle = "a".repeat(100);
        Task task = createValidTask(maxLengthTitle, "Description", false);

        // When
        taskMapper.insert(task);

        // Then
        Task foundTask = taskMapper.findById(task.getId());
        assertThat(foundTask.getTitle()).hasSize(100);
    }

    @Test
    @DisplayName("説明が500文字の場合、正常に作成できる")
    void testInsert_DescriptionMaxLength() {
        // Given
        String maxLengthDescription = "a".repeat(500);
        Task task = createValidTask("Title", maxLengthDescription, false);

        // When
        taskMapper.insert(task);

        // Then
        Task foundTask = taskMapper.findById(task.getId());
        assertThat(foundTask.getDescription()).hasSize(500);
    }

    @Test
    @DisplayName("説明がnullの場合でも作成できる")
    void testInsert_DescriptionNull() {
        // Given
        Task task = createValidTask("Title", null, false);

        // When
        taskMapper.insert(task);

        // Then
        Task foundTask = taskMapper.findById(task.getId());
        assertThat(foundTask.getDescription()).isNull();
    }

    @Test
    @DisplayName("completedフラグがfalseで作成できる")
    void testInsert_CompletedFalse() {
        // Given
        Task task = createValidTask("Title", "Description", false);

        // When
        taskMapper.insert(task);

        // Then
        Task foundTask = taskMapper.findById(task.getId());
        assertThat(foundTask.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("completedフラグがtrueで作成できる")
    void testInsert_CompletedTrue() {
        // Given
        Task task = createValidTask("Title", "Description", true);

        // When
        taskMapper.insert(task);

        // Then
        Task foundTask = taskMapper.findById(task.getId());
        assertThat(foundTask.getCompleted()).isTrue();
    }

    // ========================================
    // カスタムメソッドのテスト
    // ========================================

    @Test
    @DisplayName("完了状態がfalseのタスクを検索できる")
    void testFindByCompleted_False() {
        // Given
        Task task1 = createValidTask("Task 1", "Description 1", false);
        Task task2 = createValidTask("Task 2", "Description 2", true);
        Task task3 = createValidTask("Task 3", "Description 3", false);
        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        // When
        List<Task> incompleteTasks = taskMapper.findByCompleted(false);

        // Then
        assertThat(incompleteTasks).hasSize(2);
        assertThat(incompleteTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 3");
        assertThat(incompleteTasks).allMatch(task -> !task.getCompleted());
    }

    @Test
    @DisplayName("完了状態がtrueのタスクを検索できる")
    void testFindByCompleted_True() {
        // Given
        Task task1 = createValidTask("Task 1", "Description 1", false);
        Task task2 = createValidTask("Task 2", "Description 2", true);
        Task task3 = createValidTask("Task 3", "Description 3", true);
        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        // When
        List<Task> completedTasks = taskMapper.findByCompleted(true);

        // Then
        assertThat(completedTasks).hasSize(2);
        assertThat(completedTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 2", "Task 3");
        assertThat(completedTasks).allMatch(Task::getCompleted);
    }

    @Test
    @DisplayName("該当する完了状態のタスクがない場合、空のリストを返す")
    void testFindByCompleted_Empty() {
        // Given
        Task task = createValidTask("Task 1", "Description 1", false);
        taskMapper.insert(task);

        // When
        List<Task> completedTasks = taskMapper.findByCompleted(true);

        // Then
        assertThat(completedTasks).isEmpty();
    }

    @Test
    @DisplayName("タイトルで部分一致検索ができる")
    void testFindByTitleContaining() {
        // Given
        Task task1 = createValidTask("Buy groceries", "Milk and bread", false);
        Task task2 = createValidTask("Buy tickets", "Concert tickets", false);
        Task task3 = createValidTask("Read book", "Science fiction", false);
        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        // When
        List<Task> buyTasks = taskMapper.findByTitleContaining("Buy");

        // Then
        assertThat(buyTasks).hasSize(2);
        assertThat(buyTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Buy groceries", "Buy tickets");
    }

    @Test
    @DisplayName("タイトルで部分一致検索（大文字小文字を区別する）")
    void testFindByTitleContaining_CaseSensitive() {
        // Given
        Task task1 = createValidTask("Buy groceries", "Milk and bread", false);
        Task task2 = createValidTask("buy tickets", "Concert tickets", false);
        taskMapper.insert(task1);
        taskMapper.insert(task2);

        // When
        List<Task> buyTasks = taskMapper.findByTitleContaining("buy");

        // Then
        assertThat(buyTasks).hasSize(1);
        assertThat(buyTasks.get(0).getTitle()).isEqualTo("buy tickets");
    }

    @Test
    @DisplayName("タイトルで部分一致検索で該当なしの場合、空のリストを返す")
    void testFindByTitleContaining_Empty() {
        // Given
        Task task = createValidTask("Buy groceries", "Milk and bread", false);
        taskMapper.insert(task);

        // When
        List<Task> tasks = taskMapper.findByTitleContaining("Sell");

        // Then
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("作成日時で降順に並び替えて取得できる")
    void testFindAllOrderByCreatedAtDesc() throws InterruptedException {
        // Given
        Task task1 = createValidTask("Task 1", "Description 1", false);
        task1.setCreatedAt(LocalDateTime.now().minusHours(3));
        task1.setUpdatedAt(LocalDateTime.now().minusHours(3));
        taskMapper.insert(task1);

        Thread.sleep(10); // タイムスタンプの差を確保

        Task task2 = createValidTask("Task 2", "Description 2", false);
        task2.setCreatedAt(LocalDateTime.now().minusHours(2));
        task2.setUpdatedAt(LocalDateTime.now().minusHours(2));
        taskMapper.insert(task2);

        Thread.sleep(10);

        Task task3 = createValidTask("Task 3", "Description 3", false);
        task3.setCreatedAt(LocalDateTime.now().minusHours(1));
        task3.setUpdatedAt(LocalDateTime.now().minusHours(1));
        taskMapper.insert(task3);

        // When
        List<Task> tasks = taskMapper.findAllOrderByCreatedAtDesc();

        // Then
        assertThat(tasks).hasSize(3);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 3");
        assertThat(tasks.get(1).getTitle()).isEqualTo("Task 2");
        assertThat(tasks.get(2).getTitle()).isEqualTo("Task 1");
        // 降順であることを確認
        assertThat(tasks.get(0).getCreatedAt()).isAfter(tasks.get(1).getCreatedAt());
        assertThat(tasks.get(1).getCreatedAt()).isAfter(tasks.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("作成日時で昇順に並び替えて取得できる")
    void testFindAllOrderByCreatedAtAsc() throws InterruptedException {
        // Given
        Task task1 = createValidTask("Task 1", "Description 1", false);
        task1.setCreatedAt(LocalDateTime.now().minusHours(3));
        task1.setUpdatedAt(LocalDateTime.now().minusHours(3));
        taskMapper.insert(task1);

        Thread.sleep(10);

        Task task2 = createValidTask("Task 2", "Description 2", false);
        task2.setCreatedAt(LocalDateTime.now().minusHours(2));
        task2.setUpdatedAt(LocalDateTime.now().minusHours(2));
        taskMapper.insert(task2);

        Thread.sleep(10);

        Task task3 = createValidTask("Task 3", "Description 3", false);
        task3.setCreatedAt(LocalDateTime.now().minusHours(1));
        task3.setUpdatedAt(LocalDateTime.now().minusHours(1));
        taskMapper.insert(task3);

        // When
        List<Task> tasks = taskMapper.findAllOrderByCreatedAtAsc();

        // Then
        assertThat(tasks).hasSize(3);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 1");
        assertThat(tasks.get(1).getTitle()).isEqualTo("Task 2");
        assertThat(tasks.get(2).getTitle()).isEqualTo("Task 3");
        // 昇順であることを確認
        assertThat(tasks.get(0).getCreatedAt()).isBefore(tasks.get(1).getCreatedAt());
        assertThat(tasks.get(1).getCreatedAt()).isBefore(tasks.get(2).getCreatedAt());
    }
}
