# Task Management System API

Hệ thống RESTful API Quản lý Công việc & Dự án được xây dựng bằng **Java 21** & **Spring Boot**, tích hợp **PostgreSQL**, **Redis**, **RabbitMQ** và **Docker**.

---

## Công Nghệ Sử Dụng (Tech Stack)

- **Core**: Java 21, Spring Boot 4.1.0 (Spring Data JPA, Spring Security)
- **Auth**: JWT (Access Token + Refresh Token Cookie, Token Rotation)
- **Database**: PostgreSQL 16
- **Cache**: Redis 7 (Cache-Aside pattern)
- **Message Queue**: RabbitMQ 3 (Gửi Email thông báo bất đồng bộ)
- **Doc & Test**: Swagger UI, JUnit 5, JaCoCo (Coverage $\ge 80\%$)
- **DevOps**: Docker, Docker Compose

---

## Tính Năng Chính

- **Xác thực & Phân quyền**: Đăng ký, Đăng nhập, Token Rotation, Phân quyền Role (System/Project).
- **Quản lý Dự án & Thành viên**: CRUD dự án, thêm/xóa thành viên, phân vai trò (`OWNER`, `MEMBER`).
- **Quản lý Công việc (Task)**: Phân công, cập nhật trạng thái (`TODO`, `IN_PROGRESS`, `DONE`), bộ lọc động (`spring-filter`).
- **Nhật ký hoạt động (Activity Log)**: Tự động lưu lịch sử chuyển trạng thái task.
- **Email bất đồng bộ**: Đẩy sự kiện qua RabbitMQ gửi email HTML (Thymeleaf).

## Cài Đặt & Chạy Ứng Dụng

### 1. Chạy nhanh bằng Docker Compose

```bash
cp .env.example .env
docker-compose up -d --build
```

- **Backend API**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **RabbitMQ UI**: `http://localhost:15672` (`guest`/`guest`)

### 2. Chạy Local (Maven)

````bash
# 1. Start Postgres, Redis, RabbitMQ
docker-compose up -d postgres redis rabbitmq

# 2. Run App
.\mvnw.cmd spring-boot:run


##  Các API Chính

| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản |
| `POST` | `/api/v1/auth/login` | Đăng nhập |
| `POST` | `/api/v1/auth/refresh` | Làm mới Access Token |
| `GET` | `/api/v1/project` | Danh sách dự án cá nhân |
| `POST` | `/api/v1/project` | Tạo dự án |
| `POST` | `/api/v1/projects/{projectId}/members` | Thêm thành viên dự án |
| `GET` | `/api/v1/projects/{projectId}/tasks` | Danh sách Task trong dự án |
| `POST` | `/api/v1/projects/{projectId}/tasks` | Tạo Task |
| `PATCH`| `/api/v1/projects/{projectId}/tasks/{taskId}/status` | Cập nhật trạng thái Task |
| `GET` | `/api/v1/projects/{projectId}/tasks/{taskId}/activities` | Xem lịch sử hoạt động Task |

---

## Testing & Coverage

```bash
./mvnw clean verify
````

Báo cáo JaCoCo HTML được tạo tại `target/site/jacoco/index.html` (Đảm bảo coverage $\ge 80\%$).
