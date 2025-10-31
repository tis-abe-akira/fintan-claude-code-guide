package com.example.taskapi.mapper;

import com.example.taskapi.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    /**
     * 全てのタスクを取得
     */
    List<Task> findAll();

    /**
     * IDでタスクを取得
     */
    Task findById(@Param("id") Long id);

    /**
     * タスクを作成
     */
    void insert(Task task);

    /**
     * タスクを更新
     */
    void update(Task task);

    /**
     * タスクを削除
     */
    void deleteById(@Param("id") Long id);

    /**
     * 完了状態でタスクを検索
     */
    List<Task> findByCompleted(@Param("completed") Boolean completed);

    /**
     * タイトルで部分一致検索
     */
    List<Task> findByTitleContaining(@Param("title") String title);

    /**
     * 作成日時で並び替えて全タスクを取得（降順）
     */
    List<Task> findAllOrderByCreatedAtDesc();

    /**
     * 作成日時で並び替えて全タスクを取得（昇順）
     */
    List<Task> findAllOrderByCreatedAtAsc();
}
