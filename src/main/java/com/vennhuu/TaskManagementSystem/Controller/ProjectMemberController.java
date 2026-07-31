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
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Project Member", description = "Quản lý thành viên trong project: thêm, xóa, thay đổi vai trò")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @PostMapping("/{projectId}/members")
    @APIMessage("Add member")
    @Operation(summary = "Thêm thành viên vào project", description = "OWNER thêm thành viên bằng email. User phải tồn tại trong hệ thống và chưa là thành viên của project.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Thêm thành viên thành công"),
        @ApiResponse(responseCode = "403", description = "Chỉ OWNER mới được thêm thành viên"),
        @ApiResponse(responseCode = "404", description = "Project hoặc email không tồn tại"),
        @ApiResponse(responseCode = "409", description = "User đã là thành viên của project")
    })
    public ResponseEntity<MemberResponse> addMember(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Valid @RequestBody MemberReq member
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectMemberService.addMember(projectId, member));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @APIMessage("Remove member")
    @Operation(summary = "Xóa thành viên khỏi project", description = "OWNER xóa một thành viên. Không thể xóa chính OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
        @ApiResponse(responseCode = "400", description = "Không thể xóa OWNER khỏi project"),
        @ApiResponse(responseCode = "403", description = "Chỉ OWNER mới được xóa thành viên"),
        @ApiResponse(responseCode = "404", description = "User không thuộc project này")
    })
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Parameter(description = "ID của user cần xóa") @PathVariable Long userId
    ) {
        projectMemberService.deleteMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}/members/{userId}")
    @APIMessage("Update role member")
    @Operation(summary = "Cập nhật vai trò thành viên", description = "OWNER thay đổi vai trò (MEMBER / OWNER) của một thành viên trong project.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật vai trò thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Chỉ OWNER mới được cập nhật vai trò"),
        @ApiResponse(responseCode = "404", description = "User không thuộc project này")
    })
    public ResponseEntity<MemberResponse> updateRoleMember(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Parameter(description = "ID của user cần cập nhật") @PathVariable Long userId,
            @Valid @RequestBody MemberReq member
    ) {
        return ResponseEntity.ok(projectMemberService.updateRoleMember(projectId, userId, member));
    }

    @GetMapping("/{projectId}/members")
    @APIMessage("Get all members")
    @Operation(summary = "Lấy danh sách thành viên", description = "Lấy tất cả thành viên trong project với phân trang và bộ lọc động.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "404", description = "Project không tồn tại")
    })
    public ResponseEntity<ResultPaginationDTO> getAllMembers(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Filter Specification<ProjectMember> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(projectMemberService.getAllMembers(projectId, spec, pageable));
    }
}
