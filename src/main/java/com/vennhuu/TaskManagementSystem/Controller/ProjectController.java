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
import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.req.project.ProjectReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Service.ProjectService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/project")
@Tag(name = "Project", description = "Quản lý project: tạo, xem, cập nhật và xóa")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("")
    @APIMessage("Get all my projects")
    @Operation(summary = "Lấy danh sách project của tôi", description = "Lấy tất cả project mà user hiện tại là thành viên, hỗ trợ phân trang và lọc động.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<ResultPaginationDTO> getMyProjects(
            @Filter Specification<Project> spec,
            Pageable pageable) {
        return ResponseEntity.ok(this.projectService.getMyProjects(spec, pageable));
    }

    @GetMapping("/{id}")
    @APIMessage("Get project detail")
    @Operation(summary = "Lấy chi tiết project", description = "Lấy thông tin chi tiết một project. Kết quả được cache bởi Redis (Cache-Aside). Chỉ thành viên mới được xem.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "403", description = "Không phải thành viên của project"),
        @ApiResponse(responseCode = "404", description = "Project không tồn tại")
    })
    public ResponseEntity<ProjectResponse> getProject(
            @Parameter(description = "ID của project") @PathVariable Long id) {
        return ResponseEntity.ok(this.projectService.getProject(id));
    }

    @PostMapping("")
    @APIMessage("Create project")
    @Operation(summary = "Tạo project mới", description = "Tạo project mới. Người tạo sẽ tự động trở thành OWNER của project.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo project thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @APIMessage("Update project")
    @Operation(summary = "Cập nhật project", description = "Cập nhật thông tin project. Chỉ OWNER mới có quyền thực hiện.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật (không phải OWNER)"),
        @ApiResponse(responseCode = "404", description = "Project không tồn tại")
    })
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "ID của project") @PathVariable Long id,
            @Valid @RequestBody ProjectReq request) {
        return ResponseEntity.ok(this.projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @APIMessage("Delete project")
    @Operation(summary = "Xóa project", description = "Xóa project cùng toàn bộ task và thành viên. Chỉ OWNER mới có quyền thực hiện.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
        @ApiResponse(responseCode = "403", description = "Không có quyền xóa (không phải OWNER)"),
        @ApiResponse(responseCode = "404", description = "Project không tồn tại")
    })
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID của project") @PathVariable Long id) {
        this.projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
