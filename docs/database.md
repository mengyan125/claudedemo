# 数据库说明文档

## 一、环境信息

| 项目 | 开发环境（H2） | 本地 MySQL |
|------|---------------|------------|
| 数据库类型 | H2 内存数据库（MySQL 兼容模式） | MySQL 5.7.30 |
| 连接地址 | 自动内嵌 | localhost:3306 |
| 用户名 | sa | root |
| 密码 | （空） | 123456 |
| 数据库名 | feedbackdb | feedbackdb |
| 字符集 | — | utf8mb4 |
| Spring Profile | dev | mysql |
| 数据持久化 | 否（重启丢失） | 是 |

## 二、启动方式

```bash
# H2 内存库（默认，每次重启自动重建）
cd src/backend
JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 本地 MySQL（数据持久化）
cd src/backend
JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 三、配置文件

| 文件 | 说明 |
|------|------|
| `src/backend/src/main/resources/application.yml` | 主配置（默认 dev profile） |
| `src/backend/src/main/resources/application-dev.yml` | H2 开发环境 |
| `src/backend/src/main/resources/application-mysql.yml` | 本地 MySQL 环境 |
| `src/backend/src/main/resources/application-tidb.yml` | TiDB 生产环境（待配置） |

## 四、SQL 脚本

位于 `src/backend/src/main/resources/db/`，按顺序执行：

| 脚本 | 说明 |
|------|------|
| V1__create_sys_tables.sql | 系统表（用户、角色、用户角色关联） |
| V2__create_base_tables.sql | 基础数据表（学期、年级、班级、师生关联） |
| V3__create_feedback_tables.sql | 反馈业务表（类别、反馈、回复、附件等） |
| V4__init_data.sql | 初始化数据（测试账号、角色、学期、类别等） |

## 五、表结构总览（共 17 张表）

### 系统模块

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| sys_user | 用户表 | username, password(BCrypt), real_name, user_type(student/teacher/admin), status, deleted(逻辑删除) |
| sys_role | 角色表 | role_name, role_code(SYSTEM_ADMIN/ROLE_ADMIN/CATEGORY_ADMIN) |
| sys_user_role | 用户角色关联 | user_id, role_id |

### 基础数据模块

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| base_semester | 学期表 | semester_name, start_date, end_date, is_current |
| base_grade | 年级表 | grade_name, sort_order |
| base_class | 班级表 | class_name, grade_id(FK) |
| base_student_class | 学生班级关联 | student_id, class_id, semester_id |
| base_teacher_class | 教师任课关联 | teacher_id, class_id, semester_id, subject |

### 反馈业务模块

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| fb_category | 反馈类别 | name, is_teaching_related, sort_order, status |
| fb_category_admin | 类别管理员权限 | category_id, user_id |
| fb_feedback | 反馈主表 | category_id, student_id, teacher_id, title, content, is_anonymous, status(draft/submitted/replied), has_unread_reply, has_unread_for_admin |
| fb_feedback_attachment | 反馈附件 | feedback_id, file_name, file_path, file_type, file_size |
| fb_reply | 反馈回复 | feedback_id, reply_user_id, content |
| fb_collection | 收藏 | user_id, feedback_id |
| fb_reminder | 备注提醒 | feedback_id, sender_id, content |
| fb_reminder_receiver | 提醒接收人 | reminder_id, receiver_id, is_read |
| fb_quick_reply | 快捷回复模板 | content, create_user_id, sort_order, is_active |

## 六、初始化账号

| 用户名 | 密码 | 真实姓名 | 类型 | 角色 |
|--------|------|----------|------|------|
| admin | 123456 | 系统管理员 | admin | SYSTEM_ADMIN |
| teacher1 | 123456 | 张老师 | teacher | — |
| teacher2 | 123456 | 李老师 | teacher | — |
| teacher3 | 123456 | 王老师 | teacher | — |
| student1 | 123456 | 赵同学 | student | — |
| student2 | 123456 | 钱同学 | student | — |

## 七、MySQL 手动初始化步骤

如需在新的 MySQL 实例上初始化：

```bash
# 1. 创建数据库
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS feedbackdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 执行建表脚本（注意指定字符集）
mysql -u root -p123456 --default-character-set=utf8mb4 feedbackdb < src/backend/src/main/resources/db/V1__create_sys_tables.sql
mysql -u root -p123456 --default-character-set=utf8mb4 feedbackdb < src/backend/src/main/resources/db/V2__create_base_tables.sql
mysql -u root -p123456 --default-character-set=utf8mb4 feedbackdb < src/backend/src/main/resources/db/V3__create_feedback_tables.sql
mysql -u root -p123456 --default-character-set=utf8mb4 feedbackdb < src/backend/src/main/resources/db/V4__init_data.sql
```

## 八、注意事项

1. **密码加密**：用户密码使用 BCrypt 加密存储，V4 中所有测试账号密码均为 `123456` 的 BCrypt 值
2. **逻辑删除**：sys_user 表使用 `deleted` 字段实现逻辑删除（0=未删除，1=已删除）
3. **ID 策略**：所有表主键使用 BIGINT AUTO_INCREMENT
4. **字符集**：MySQL 客户端连接时需指定 `--default-character-set=utf8mb4`，JDBC URL 中使用 `characterEncoding=UTF-8`
5. **无迁移工具**：未使用 Flyway/Liquibase，SQL 脚本文件名采用 Flyway 风格但通过 Spring Boot sql.init 机制加载
