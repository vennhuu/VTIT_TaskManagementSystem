package com.vennhuu.TaskManagementSystem.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vennhuu.TaskManagementSystem.Entity.req.member.MemberReq;
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
    
    @DeleteMapping("/{projectId}/members/{memberId}")
    @APIMessage("Remove member")
    public ResponseEntity<Void> removeMember( @PathVariable Long projectId, @PathVariable Long memberId ) {

        this.projectMemberService.deleteMember(projectId, memberId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{projectId}/members/{memberId}")
    @APIMessage("Update role member")
    public ResponseEntity<MemberResponse> updateRoleMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody MemberReq member) {

        MemberResponse response = this.projectMemberService.updateRoleMember(projectId, memberId, member);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/members")
    @APIMessage("Get all members")
    public ResponseEntity<List<MemberResponse>> getAllMembers(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(this.projectMemberService.getAllMembers(projectId));
    }
    
    
}
