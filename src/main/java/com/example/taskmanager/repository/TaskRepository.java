package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTitleContainingIgnoreCaseOrderByDueDateAsc(String title);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByDueDateBefore(LocalDate date);

    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<Task> findAllByOrderByPriorityDescDueDateAsc();
}