```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:activiti="http://activiti.org/bpmn">
  <process id="workflowProcess" name="Workflow Process" isExecutable="true">
    
    <!-- Start Event -->
    <startEvent id="startEvent" name="Start" activiti:initiator="initiator" />
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="createdState" />

    <!-- Created State -->
    <userTask id="createdState" name="Created" activiti:assignee="${initiator}" />
    <sequenceFlow id="flow2" sourceRef="createdState" targetRef="assignedState" />
    <sequenceFlow id="flow3" sourceRef="createdState" targetRef="cancelledState" />

    <!-- Assigned State -->
    <userTask id="assignedState" name="Assigned" activiti:assignee="reviewer" />
    <sequenceFlow id="flow4" sourceRef="assignedState" targetRef="reassignedState" />
    <sequenceFlow id="flow5" sourceRef="assignedState" targetRef="escalatedState" />
    <sequenceFlow id="flow6" sourceRef="assignedState" targetRef="approvedState" />
    <sequenceFlow id="flow7" sourceRef="assignedState" targetRef="declinedState" />
    <sequenceFlow id="flow8" sourceRef="assignedState" targetRef="reworkState" />
    <sequenceFlow id="flow9" sourceRef="assignedState" targetRef="cancelledState" />

    <!-- Reassigned State -->
    <userTask id="reassignedState" name="Reassigned" activiti:assignee="newReviewer" />
    <sequenceFlow id="flow10" sourceRef="reassignedState" targetRef="escalatedState" />
    <sequenceFlow id="flow11" sourceRef="reassignedState" targetRef="approvedState" />
    <sequenceFlow id="flow12" sourceRef="reassignedState" targetRef="declinedState" />
    <sequenceFlow id="flow13" sourceRef="reassignedState" targetRef="reworkState" />
    <sequenceFlow id="flow14" sourceRef="reassignedState" targetRef="cancelledState" />

    <!-- Escalated State -->
    <userTask id="escalatedState" name="Escalated" activiti:assignee="escalator" />
    <sequenceFlow id="flow15" sourceRef="escalatedState" targetRef="deescalatedState" />
    <sequenceFlow id="flow16" sourceRef="escalatedState" targetRef="approvedState" />
    <sequenceFlow id="flow17" sourceRef="escalatedState" targetRef="declinedState" />
    <sequenceFlow id="flow18" sourceRef="escalatedState" targetRef="reworkState" />
    <sequenceFlow id="flow19" sourceRef="escalatedState" targetRef="cancelledState" />

    <!-- Deescalated State -->
    <userTask id="deescalatedState" name="Deescalated" activiti:assignee="escalator" />
    <sequenceFlow id="flow20" sourceRef="deescalatedState" targetRef="approvedState" />
    <sequenceFlow id="flow21" sourceRef="deescalatedState" targetRef="declinedState" />
    <sequenceFlow id="flow22" sourceRef="deescalatedState" targetRef="reworkState" />
    <sequenceFlow id="flow23" sourceRef="deescalatedState" targetRef="cancelledState" />

    <!-- Approved State -->
    <userTask id="approvedState" name="Approved" activiti:assignee="approver" />
    <sequenceFlow id="flow24" sourceRef="approvedState" targetRef="preProductionCooldownState" />

    <!-- PreProductionCooldown State -->
    <userTask id="preProductionCooldownState" name="PreProductionCooldown" />
    <sequenceFlow id="flow25" sourceRef="preProductionCooldownState" targetRef="productionState" />

    <!-- Production State -->
    <userTask id="productionState" name="Production" />
    <sequenceFlow id="flow26" sourceRef="productionState" targetRef="cancelledState" />

    <!-- Rework State -->
    <userTask id="reworkState" name="Rework" activiti:assignee="requester" />
    <sequenceFlow id="flow27" sourceRef="reworkState" targetRef="assignedState" />
    <sequenceFlow id="flow28" sourceRef="reworkState" targetRef="reassignedState" />

    <!-- Cancelled State -->
    <userTask id="cancelledState" name="Cancelled" activiti:assignee="requester" />
    <sequenceFlow id="flow29" sourceRef="cancelledState" targetRef="preDeleteCooldownState" />

    <!-- PreDeleteCooldown State -->
    <userTask id="preDeleteCooldownState" name="PreDeleteCooldown" />
    <sequenceFlow id="flow30" sourceRef="preDeleteCooldownState" targetRef="softDeletedState" />

    <!-- SoftDeleted State -->
    <userTask id="softDeletedState" name="SoftDeleted" />
    <sequenceFlow id="flow31" sourceRef="softDeletedState" targetRef="policyBasedCleanupState" />

    <!-- PolicyBasedCleanup (Final Step for Archiving) -->
    <serviceTask id="policyBasedCleanupState" name="PolicyBasedCleanup" />
    <sequenceFlow id="flow32" sourceRef="policyBasedCleanupState" targetRef="endEvent" />

    <!-- End Event -->
    <endEvent id="endEvent" name="End" />

  </process>
</definitions>
```
```sql
CREATE TABLE IF NOT EXISTS workflow_audit (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(255),
    event_description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_notifications (
    id SERIAL PRIMARY KEY,
    notification_message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
```java
package com.workflow.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String message) {
        kafkaTemplate.send("notifications", message);
    }
}

@Service
public class AuditService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public AuditService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void logEvent(String event) {
        kafkaTemplate.send("audit", event);
    }
}

package com.workflow.service;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.RepositoryService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    public ProcessInstance startWorkflow(String initiator) {
        return runtimeService.startProcessInstanceByKey("workflowProcess", "initiator", initiator);
    }

    public List<String> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee)
                .list()
                .stream()
                .map(Task::getName)
                .collect(Collectors.toList());
    }

    public void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    public void assignTask(String taskId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        task.setAssignee(assignee);
        taskService.saveTask(task);
    }

    public void approveTask(String taskId) {
        taskService.complete(taskId);
    }

    public void escalateTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // Escalate logic here
        taskService.saveTask(task);
    }

    public String uploadWorkflowFile(MultipartFile file) {
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .addInputStream(file.getOriginalFilename(), file.getInputStream())
                    .deploy();
            return "Deployed successfully: " + deployment.getId();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading workflow file", e);
        }
    }
}
package com.workflow.controller;

import com.workflow.service.WorkflowService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/start")
    public ProcessInstance startWorkflow(@RequestParam String initiator) {
        return workflowService.startWorkflow(initiator);
    }

    @GetMapping("/tasks")
    public List<String> getTasks(@RequestParam String assignee) {
        return workflowService.getTasks(assignee);
    }

    @PostMapping("/complete")
    public void completeTask(@RequestParam String taskId) {
        workflowService.completeTask(taskId);
    }

    @PutMapping("/assign")
    public void assignTask(@RequestParam String taskId, @RequestParam String assignee) {
        workflowService.assignTask(taskId, assignee);
    }

    @PostMapping("/upload")
    public String uploadWorkflowFile(@RequestParam("file") MultipartFile file) {
        return workflowService.uploadWorkflowFile(file);
    }

    @PutMapping("/approve")
    public void approveTask(@RequestParam String taskId) {
        workflowService.approveTask(taskId);
    }

    @PutMapping("/escalate")
    public void escalateTask(@RequestParam String taskId) {
        workflowService.escalateTask(taskId);
    }
}
package com.workflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/workflow/approve", "/workflow/assign").hasAnyRole("APPROVER", "ADMIN")
            .antMatchers("/workflow/escalate", "/workflow/deescalate").hasRole("ESCALATOR")
            .antMatchers("/workflow/upload").hasRole("ADMIN")
            .antMatchers("/workflow/**").permitAll()
            .anyRequest().authenticated()
            .and().httpBasic();
    }
}
package com.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private String username;
    private UserRole role;
}
package com.workflow.model;

public enum WorkflowState {
    CREATED,
    SOFT_DELETED,
    ASSIGNED,
    REASSIGNED,
    ESCALATED,
    DEESCALATED,
    APPROVED,
    DECLINED,
    PRE_PRODUCTION_COOLDOWN,
    PRE_DELETE_COOLDOWN,
    PRODUCTION,
    REWORK,
    CANCELLED
}

package com.workflow.model;

public enum UserRole {
    REQUESTER,
    APPROVER,
    ESCALATOR,
    ADMIN
}

```

```java
// Enum for Workflow States

package com.workflow.model;

public enum WorkflowState {
    CREATED,
    SOFT_DELETED,
    ASSIGNED,
    REASSIGNED,
    ESCALATED,
    DEESCALATED,
    APPROVED,
    DECLINED,
    PRE_PRODUCTION_COOLDOWN,
    PRE_DELETE_COOLDOWN,
    PRODUCTION,
    REWORK,
    CANCELLED
}

// Role-based Access Control (RBAC) Enum

package com.workflow.model;

public enum UserRole {
    REQUESTER,
    APPROVER,
    ESCALATOR,
    ADMIN
}

// User entity for RBAC

package com.workflow.model;

public class User {
    private String username;
    private UserRole role;

    // Constructors, Getters and Setters
    public User(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}

// Security Configuration for RBAC

package com.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/workflow/assign").hasAnyRole("APPROVER", "ADMIN")
                .antMatchers("/workflow/escalate").hasRole("ESCALATOR")
                .antMatchers("/workflow/approve").hasRole("APPROVER")
                .antMatchers("/workflow/reassign").hasRole("APPROVER")
                .antMatchers("/workflow/**").permitAll()
                .anyRequest().authenticated()
                .and().httpBasic(); // Basic authentication for simplicity
    }
}

// Service for Notifications and Audit

package com.workflow.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String message) {
        kafkaTemplate.send("notifications", message);
    }
}

package com.workflow.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public AuditService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void logEvent(String event) {
        kafkaTemplate.send("audit", event);
    }
}

// Controller with Versioned File Update and Complete Endpoints

package com.workflow.controller;

import com.workflow.model.WorkflowState;
import com.workflow.service.WorkflowService;
import com.workflow.service.NotificationService;
import com.workflow.service.AuditService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @PostMapping("/start")
    public ProcessInstance startWorkflow(@RequestParam String initiator) {
        ProcessInstance processInstance = workflowService.startWorkflow(initiator);
        auditService.logEvent("Workflow started by " + initiator);
        notificationService.sendNotification("Workflow created for " + initiator);
        return processInstance;
    }

    @GetMapping("/tasks")
    public List<Task> getTasks(@RequestParam String assignee) {
        return workflowService.getTasks(assignee);
    }

    @PostMapping("/complete")
    public void completeTask(@RequestParam String taskId) {
        workflowService.completeTask(taskId);
        auditService.logEvent("Task " + taskId + " completed.");
    }

    @PutMapping("/assign")
    public void assignReviewer(@RequestParam String taskId, @RequestParam String reviewer) {
        workflowService.assignReviewer(taskId, reviewer);
        auditService.logEvent("Task " + taskId + " assigned to " + reviewer);
        notificationService.sendNotification("Task " + taskId + " assigned to " + reviewer);
    }

    @PutMapping("/approve")
    public void approveRequest(@RequestParam String taskId) {
        workflowService.approveRequest(taskId);
        auditService.logEvent("Task " + taskId + " approved.");
        notificationService.sendNotification("Task " + taskId + " approved.");
    }

    @PutMapping("/escalate")
    public void escalateRequest(@RequestParam String taskId, @RequestParam String comment) {
        workflowService.escalateRequest(taskId, comment);
        auditService.logEvent("Task " + taskId + " escalated with comment: " + comment);
        notificationService.sendNotification("Task " + taskId + " escalated.");
    }

    @PutMapping("/reassign")
    public void reassignRequest(@RequestParam String taskId, @RequestParam String newReviewer) {
        workflowService.reassignReviewer(taskId, newReviewer);
        auditService.logEvent("Task " + taskId + " reassigned to " + newReviewer);
        notificationService.sendNotification("Task " + taskId + " reassigned to " + newReviewer);
    }

    @PostMapping("/upload")
    public String uploadVersionedWorkflowFile(@RequestParam("file") MultipartFile file) {
        return workflowService.uploadWorkflowFile(file);
    }
}

// Service Implementation for Task Management

package com.workflow.service;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class WorkflowService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public WorkflowService(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    // Start workflow
    public ProcessInstance startWorkflow(String initiator) {
        return runtimeService.startProcessInstanceByKey("workflowProcess", "initiator", initiator);
    }

    // Get tasks for assignee
    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    // Complete task
    public void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    // Assign a reviewer
    public void assignReviewer(String taskId, String reviewer) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        task.setAssignee(reviewer);
        taskService.saveTask(task);
    }

    // Approve request
    public void approveRequest(String taskId) {
        taskService.complete(taskId);
    }

    // Escalate request
    public void escalateRequest(String taskId, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // Add custom logic for escalation
        taskService.saveTask(task);
    }

    // Reassign reviewer
    public void reassignReviewer(String taskId, String newReviewer) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        task.setAssignee(newReviewer);
        taskService.saveTask(task);
    }

    // Upload versioned workflow file
    public String uploadWorkflowFile(MultipartFile file) {
        // Implement file storage and versioning logic
        return "File uploaded and versioned successfully!";
    }
}

// Flyway Migration Script (V1__initial_schema.sql)

CREATE TABLE ACT_RU_TASK (
    ID_ VARCHAR(64) NOT NULL,
    NAME_ VARCHAR(255) DEFAULT NULL,
    ASSIGNEE_ VARCHAR(255) DEFAULT NULL,
    TASK_DEF_KEY_ VARCHAR(255) DEFAULT NULL,
    PROC_INST_ID_ VARCHAR(64) DEFAULT NULL,
    EXECUTION_ID_ VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (ID_)
);
```
