package com.vennhuu.TaskManagementSystem.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vennhuu.TaskManagementSystem.Entity.res.activityLog.ActivityLogResponse;
import com.vennhuu.TaskManagementSystem.Service.ActivityLogService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@Tag(name = "Activity Log", description = "Xem lịch sử thay đổi trạng thái task trong project")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/tasks/{taskId}/activities")
    @APIMessage("Get activity logs by task")
    @Operation(summary = "Lấy lịch sử theo task", description = "Lấy toàn bộ lịch sử thay đổi trạng thái của một task cụ thể, sắp xếp theo thời gian mới nhất.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "404", description = "Task không tồn tại")
    })
    public ResponseEntity<List<ActivityLogResponse>> getLogsByTask(
            @Parameter(description = "ID của project") @PathVariable Long projectId,
            @Parameter(description = "ID của task")   @PathVariable Long taskId) {

        return ResponseEntity.ok(activityLogService.getByTask(taskId));
    }

    @GetMapping("/activities")
    @APIMessage("Get all activity logs")
    @Operation(summary = "Lấy toàn bộ lịch sử trong project", description = "Lấy tất cả lịch sử thay đổi trạng thái task thuộc project. Chỉ thành viên của project mới xem được.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "403", description = "Không phải thành viên của project"),
        @ApiResponse(responseCode = "404", description = "Project không tồn tại")
    })
    public ResponseEntity<List<ActivityLogResponse>> getAllLogs(
            @Parameter(description = "ID của project") @PathVariable Long projectId) {

        return ResponseEntity.ok(activityLogService.getAllLogByProject(projectId));
    }
}
