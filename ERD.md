# Sơ Đồ ERD (Entity Relationship Diagram) - Task Management System

Below is the database schema ERD represented using Mermaid notation.

```mermaid
erDiagram
    ROLES {
        bigint id PK
        varchar name
        text description
    }

    USERS {
        bigint id PK
        varchar full_name
        varchar email UK "Index: idx_user_email"
        varchar password
        bigint role_id FK
        timestamp created_at
        timestamp updated_at
    }

    REFRESH_TOKEN {
        bigint id PK
        varchar device
        text token "Index: idx_refresh_token"
        bigint user_id FK
        timestamp created_at
        timestamp expired_at
        boolean revoked
        timestamp revoked_at
    }

    PROJECTS {
        bigint id PK
        varchar name
        text description
        bigint created_by FK
        timestamp created_at
        timestamp updated_at
    }

    PROJECT_MEMBERS {
        bigint id PK
        bigint project_id FK "UK: (project_id, user_id)"
        bigint user_id FK "UK: (project_id, user_id)"
        varchar role
        timestamp joined_at
    }

    TASKS {
        bigint id PK
        varchar title
        text description
        varchar status "Index: idx_task_status"
        date due_date
        bigint created_by FK
        bigint project_id FK "Index: idx_task_project_id"
        bigint assignee_id FK "Index: idx_task_assignee_id"
        timestamp created_at
        timestamp updated_at
    }

    ACTIVITY_LOG {
        bigint id PK
        bigint task_id FK "Index: idx_activity_task_id"
        bigint user_id FK
        varchar from_status
        varchar to_status
        timestamp updated_at "Index: idx_activity_updated_at"
    }

    ROLES ||--o{ USERS : "has"
    USERS ||--o{ REFRESH_TOKEN : "owns"
    USERS ||--o{ PROJECTS : "creates"
    PROJECTS ||--o{ PROJECT_MEMBERS : "contains"
    USERS ||--o{ PROJECT_MEMBERS : "belongs_to"
    PROJECTS ||--o{ TASKS : "contains"
    USERS ||--o{ TASKS : "creates/assigns"
    TASKS ||--o{ ACTIVITY_LOG : "logs"
    USERS ||--o{ ACTIVITY_LOG : "performs"
```

## Ghi chú về Chuẩn Hóa & Đánh Index
1. **Chuẩn hóa (3NF)**:
   - Các bảng tách biệt đúng thực thể, không lặp lại nhóm dữ liệu (1NF).
   - Mọi thuộc tính không khóa đều phụ thuộc hoàn toàn vào Khóa chính (2NF).
   - Không tồn tại phụ thuộc bắc cầu giữa các thuộc tính không khóa (3NF).
2. **Đánh Index**:
   - `users.email`: Tối ưu tìm kiếm và đăng nhập theo email (`idx_user_email`).
   - `refresh_token.token`: Tối ưu truy vấn xác thực token (`idx_refresh_token`).
   - `tasks.project_id`, `tasks.assignee_id`, `tasks.status`: Tối ưu truy vấn danh sách công việc và lọc theo dự án/trạng thái/người thực hiện.
   - `activityLog.task_id`, `activityLog.updatedAt`: Tối ưu xem nhật ký hoạt động theo công việc và thời gian.
   - `project_members.(project_id, user_id)`: Ràng buộc duy nhất & tự động tạo Composite Index giúp kiểm tra quyền nhanh chóng.
