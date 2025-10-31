package com.example.taskapi.service;

import com.example.taskapi.entity.Task;
import com.example.taskapi.exception.ResourceNotFoundException;
import com.example.taskapi.mapper.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    /**
     * 全タスク取得
     */
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskMapper.findAll();
    }

    /**
     * ID指定でタスク取得
     */
    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        Task task = taskMapper.findById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        return task;
    }

    /**
     * タスク作成
     */
    public Task createTask(Task task) {
        // 作成時にcompletedがnullの場合はfalseを設定
        if (task.getCompleted() == null) {
            task.setCompleted(false);
        }

        // 作成日時と更新日時を設定
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        taskMapper.insert(task);
        return task;
    }

    /**
     * タスク更新
     */
    public Task updateTask(Long id, Task taskDetails) {
        // 既存のタスクを取得（存在しない場合は例外をスロー）
        Task existingTask = getTaskById(id);

        // 更新する項目を設定
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setCompleted(taskDetails.getCompleted());
        existingTask.setUpdatedAt(LocalDateTime.now());

        taskMapper.update(existingTask);
        return existingTask;
    }

    /**
     * タスク削除
     */
    public void deleteTask(Long id) {
        // タスクが存在することを確認
        getTaskById(id);

        taskMapper.deleteById(id);
    }

    /**
     * 完了状態の切り替え
     */
    public Task toggleTaskCompletion(Long id) {
        // 既存のタスクを取得（存在しない場合は例外をスロー）
        Task task = getTaskById(id);

        // 完了状態を切り替え
        task.setCompleted(!task.getCompleted());
        task.setUpdatedAt(LocalDateTime.now());

        taskMapper.update(task);
        return task;
    }

    /**
     * 完了状態で検索
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByCompleted(Boolean completed) {
        return taskMapper.findByCompleted(completed);
    }

    /**
     * タイトルで部分一致検索
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByTitleContaining(String title) {
        if (title == null || title.trim().isEmpty()) {
            return taskMapper.findAll();
        }
        return taskMapper.findByTitleContaining(title);
    }

    /**
     * 作成日時で並び替えて全タスクを取得（降順）
     */
    @Transactional(readOnly = true)
    public List<Task> getAllTasksOrderByCreatedAtDesc() {
        return taskMapper.findAllOrderByCreatedAtDesc();
    }

    /**
     * 作成日時で並び替えて全タスクを取得（昇順）
     */
    @Transactional(readOnly = true)
    public List<Task> getAllTasksOrderByCreatedAtAsc() {
        return taskMapper.findAllOrderByCreatedAtAsc();
    }
}
