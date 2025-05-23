package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAllByOrderByPriorityDescDueDateAsc();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional
    public Task saveTask(Task task) {
        log.debug("Sauvegarde de la tâche: {}", task.getTitle());
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.debug("Suppression de la tâche avec ID: {}", id);
        taskRepository.deleteById(id);
    }

    public List<Task> searchTasks(String query) {
        return taskRepository.findByTitleContainingIgnoreCaseOrderByDueDateAsc(query);
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findByDueDateBefore(LocalDate.now());
    }

    public List<Task> getTasksForNextWeek() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return taskRepository.findByDueDateBetween(today, nextWeek);
    }

    @Transactional
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(status);
            return taskRepository.save(task);
        }
        throw new RuntimeException("Tâche non trouvée avec l'ID: " + id);
    }
}