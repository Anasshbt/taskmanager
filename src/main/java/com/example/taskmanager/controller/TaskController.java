package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public String listTasks(Model model, @RequestParam(required = false) String query) {
        if (query != null && !query.isEmpty()) {
            model.addAttribute("tasks", taskService.searchTasks(query));
            model.addAttribute("query", query);
        } else {
            model.addAttribute("tasks", taskService.getAllTasks());
        }

        return "tasks/list";
    }

    @GetMapping("/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            model.addAttribute("task", taskOpt.get());
            model.addAttribute("statuses", TaskStatus.values());
            return "tasks/view";
        }
        return "redirect:/tasks";
    }

    @GetMapping("/new")
    public String createTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("isNew", true);
        return "tasks/form";
    }

    @PostMapping
    public String saveTask(@Valid @ModelAttribute Task task, BindingResult result,
                           RedirectAttributes redirectAttributes, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("isNew", task.getId() == null);
            return "tasks/form";
        }

        Task savedTask = taskService.saveTask(task);

        redirectAttributes.addFlashAttribute("successMessage",
                "La tâche a été " + (task.getId() == null ? "créée" : "mise à jour") + " avec succès");
        return "redirect:/tasks/" + savedTask.getId();
    }

    @GetMapping("/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            model.addAttribute("task", taskOpt.get());
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("isNew", false);
            return "tasks/form";
        }
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam TaskStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            Task updatedTask = taskService.updateTaskStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Statut de la tâche mis à jour: " + status.getDisplayName());
            return "redirect:/tasks/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la mise à jour du statut: " + e.getMessage());
            return "redirect:/tasks";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            taskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tâche supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/tasks";
    }

    @GetMapping("/filter")
    public String filterTasks(@RequestParam TaskStatus status, Model model) {
        model.addAttribute("tasks", taskService.getTasksByStatus(status));
        model.addAttribute("currentFilter", status);
        model.addAttribute("filterName", status.getDisplayName());
        return "tasks/list";
    }

    @GetMapping("/overdue")
    public String getOverdueTasks(Model model) {
        model.addAttribute("tasks", taskService.getOverdueTasks());
        model.addAttribute("currentFilter", "OVERDUE");
        model.addAttribute("filterName", "Tâches en retard");
        return "tasks/list";
    }
}