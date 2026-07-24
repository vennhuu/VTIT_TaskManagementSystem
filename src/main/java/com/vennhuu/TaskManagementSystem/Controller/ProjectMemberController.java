package com.vennhuu.TaskManagementSystem.Controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

import com.turkraft.springfilter.boot.Filter;
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.req.member.MemberReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.member.MemberResponse;
import com.vennhuu.TaskManagementSystem.Service.ProjectMemberService;
import com.vennhuu.TaskManagementSystem.Service.ProjectService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectMemberController {
    
    private final ProjectMemberService projectMemberService ;
    private final ProjectService projectService ;

    public ProjectMemberController(ProjectMemberService projectMemberService, ProjectService projectService) {
        this.projectMemberService = projectMemberService;
        this.projectService = projectService ;
    }

    @PostMapping("/{projectId}/members")
    @APIMessage("Add member")
    public ResponseEntity<MemberResponse> addMember( @PathVariable Long projectId, @Valid @RequestBody MemberReq member) {

        MemberResponse response = this.projectMemberService.addMember(projectId, member);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{projectId}/members/{userId}")
    @APIMessage("Remove member")
    public ResponseEntity<Void> removeMember( @PathVariable Long projectId, @PathVariable Long userId ) {

        this.projectMemberService.deleteMember(projectId, userId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}/members/{userId}")
    @APIMessage("Update role member")
    public ResponseEntity<MemberResponse> updateRoleMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody MemberReq member
        ) {

        MemberResponse response = this.projectMemberService.updateRoleMember(projectId, userId, member);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/members")
    @APIMessage("Get all members")
    public ResponseEntity<ResultPaginationDTO> getAllMembers(
            @PathVariable Long projectId,
            @Filter Specification<ProjectMember> spec,
            Pageable pageable
        ) {

        return ResponseEntity.ok(this.projectMemberService.getAllMembers(projectId, spec, pageable));
    }
    
    
}
