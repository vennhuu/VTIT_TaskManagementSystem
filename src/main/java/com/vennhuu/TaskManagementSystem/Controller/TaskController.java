package com.vennhuu.TaskManagementSystem.Controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.req.task.TaskReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Entity.res.task.UpdateStatus;
import com.vennhuu.TaskManagementSystem.Service.TaskService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@Tag(name = "Task", description = "Quản lý task trong một project")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @APIMessage("Create task")
    @Operation(summary = "Tạo task mới", description = "Tạo task trong project chỉ định. Người tạo phải là thành viên của project, assignee cũng phải thuộc project.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo task thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc assignee không thuộc project"),
        @ApiResponse(responseCode = "403", description = "Người dùng không thuộc project này"),
        @ApiResponse(responseCode = "404", description = "Project hoặc assignee không tồn tại")
    })
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Valid @RequestBody TaskReq req) {

        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, req));
    }

    @GetMapping
    @APIMessage("Get all task")
    @Operation(summary = "Lấy danh sách task", description = "Lấy tất cả task trong project với phân trang và bộ lọc động (Spring Filter).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<ResultPaginationDTO> getAllTask(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Filter Specification<Task> spec,
            Pageable pageable) {

        return ResponseEntity.ok(taskService.getAllTask(projectId, spec, pageable));
    }

    @GetMapping("/{taskId}")
    @APIMessage("Get task detail")
    @Operation(summary = "Lấy chi tiết task", description = "Lấy thông tin chi tiết một task. Kết quả được cache bởi Redis (Cache-Aside).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "403", description = "Người dùng không thuộc project này"),
        @ApiResponse(responseCode = "404", description = "Task không tồn tại")
    })
    public ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Parameter(description = "ID của task")   @PathVariable Long taskId) {

        return ResponseEntity.ok(taskService.getTask(projectId, taskId));
    }

    @PutMapping("/{taskId}")
    @APIMessage("Update task")
    @Operation(summary = "Cập nhật task", description = "Cập nhật toàn bộ thông tin task. Cache của task sẽ bị xóa sau khi cập nhật.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Task hoặc assignee không tồn tại")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Parameter(description = "ID của task")   @PathVariable Long taskId,
            @Valid @RequestBody TaskReq req) {

        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, req));
    }

    @PatchMapping("/{taskId}/status")
    @APIMessage("Update task status")
    @Operation(summary = "Cập nhật trạng thái task", description = "Thay đổi trạng thái task (TODO / IN_PROGRESS / DONE). Hành động được ghi vào ActivityLog.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công"),
        @ApiResponse(responseCode = "400", description = "Task không thuộc project này"),
        @ApiResponse(responseCode = "404", description = "Task không tồn tại")
    })
    public ResponseEntity<TaskResponse> updateStatus(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Parameter(description = "ID của task")   @PathVariable Long taskId,
            @RequestBody UpdateStatus req) {

        return ResponseEntity.ok(taskService.updateStatus(projectId, taskId, req.getStatus()));
    }

    @DeleteMapping("/{taskId}")
    @APIMessage("Delete task")
    @Operation(summary = "Xóa task", description = "Xóa task khỏi project. Cache của task sẽ bị xóa đồng thời.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
        @ApiResponse(responseCode = "400", description = "Task không thuộc project này"),
        @ApiResponse(responseCode = "404", description = "Task không tồn tại")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Parameter(description = "ID của task")   @PathVariable Long taskId) {

        taskService.deleteTask(projectId, taskId);

        return ResponseEntity.noContent().build();
    }
}