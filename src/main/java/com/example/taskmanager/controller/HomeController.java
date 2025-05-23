package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TaskService taskService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalTasks", taskService.getAllTasks().size());
        model.addAttribute("todoTasks", taskService.getTasksByStatus(TaskStatus.TODO).size());
        model.addAttribute("inProgressTasks", taskService.getTasksByStatus(TaskStatus.IN_PROGRESS).size());
        model.addAttribute("completedTasks", taskService.getTasksByStatus(TaskStatus.COMPLETED).size());
        model.addAttribute("overdueTasks", taskService.getOverdueTasks().size());
        model.addAttribute("upcomingTasks", taskService.getTasksForNextWeek());

        return "index";
    }
}