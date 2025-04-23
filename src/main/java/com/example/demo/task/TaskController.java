package com.example.demo.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public Task createTask(@RequestBody TaskRequestDTO dto) {
        return taskService.createTask(dto.senderId, dto.receiverId, dto.description);
    }

    @GetMapping("/receiver/{id}")
    public List<Task> getTasksForReceiver(@PathVariable Long id) {
        return taskService.getTasksForReceiver(id);
    }

    @GetMapping("/sender/{id}")
    public List<Task> getTasksFromSender(@PathVariable Long id) {
        return taskService.getTasksFromSender(id);
    }

    @PutMapping("/{id}/done")
    public Optional<Task> markAsDone(@PathVariable Long id) {
        return taskService.markTaskAsDone(id);
    }
}
