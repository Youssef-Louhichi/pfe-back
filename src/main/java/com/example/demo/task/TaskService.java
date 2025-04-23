package com.example.demo.task;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.email.EmailService;
import com.example.demo.users.User;
import com.example.demo.users.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;

    public Task createTask(Long senderId, Long receiverId, String description) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Task task = new Task();
        task.setSender(sender);
        task.setReceiver(receiver);
        task.setDescription(description);
        
        String assignmentBody = "Hello " + task.getReceiver().getMail() + ",\n\n" +
        	    "You have been assigned a new task by " + task.getSender().getMail() + ":\n\n" +
        	    "Task: " + task.getDescription() + "\n\n" +
        	    "Please log in to the system to view details.\n\n" +
        	    "Best regards,\n" +
        	    "Your Application Team";
        
        emailService.sendEmail(
        	    task.getReceiver().getMail(),
        	    "📋 New Task Assigned",
        	    assignmentBody
        	);


        return taskRepository.save(task);
    }

    public List<Task> getTasksForReceiver(Long receiverId) {
    	User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        return receiver.getReceivedTasks();
    }

    public List<Task> getTasksFromSender(Long senderId) {
    	 User sender = userRepository.findById(senderId)
                 .orElseThrow(() -> new RuntimeException("Sender not found"));
        return sender.getSentTasks();
    }

    public Optional<Task> markTaskAsDone(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        taskOpt.ifPresent(task -> {
            task.setDone(true);
            taskRepository.save(task);
            String completionBody = "Hello " + task.getSender().getMail() + ",\n\n" +
            	    "The task you assigned to " + task.getReceiver().getMail() + " has been marked as completed.\n\n" +
            	    "Task: " + task.getDescription() + "\n\n" +
            	    "Completed on: " + LocalDateTime.now() + "\n\n" +
            	    "You can review the work in the system.\n\n" +
            	    "Best regards,\n" +
            	    "Your Application Team";
            emailService.sendEmail(
            	    task.getSender().getMail(),
            	    "✅ Task Completed",
            	    completionBody
            	);

        });
        
        return taskOpt;
    }
}

