package com.vennhuu.TaskManagementSystem.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vennhuu.TaskManagementSystem.Entity.req.project.ProjectReq;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Service.ProjectService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService ;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("")
    @APIMessage("Get all my projects")
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        return ResponseEntity.ok(this.projectService.getMyProjects());
    }

    @GetMapping("/{id}")
    @APIMessage("Get project detail")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(this.projectService.getProject(id));
    }

    @PostMapping("")
    @APIMessage("Create project")
    public ResponseEntity<ProjectResponse> createProject( @Valid @RequestBody ProjectReq request ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(this.projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @APIMessage("Update project")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectReq request) {

        return ResponseEntity.ok(this.projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @APIMessage("Delete project")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {

        this.projectService.deleteProject(id);

        return ResponseEntity.noContent().build();
    }
    
}
