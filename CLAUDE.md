# Vendor Backend Development Contract

本目录是厂商端后端工程。开发时遵守上级 `AGENTS.md` 与本文约束。

## Scope

- 数据库统一 SQL Server `vendor`，不得新增旧数据库或多数据库兼容路径。
- 厂商端只写厂商端业务表，不写企业端本地业务库。
- 数据库连接信息必须通过环境变量或外部配置提供，源码不保存客户或企业数据库凭据。
- 业务代码优先复用 RuoYi 框架能力和已有项目模式。
- 保留系统基础能力：认证、RBAC、租户套餐、用户、角色、部门、岗位、公告、操作日志、登录日志、文件、Excel、缓存基础设施。
- 不恢复已剔除的 RuoYi 原生交付外能力：代码生成、任务调度、工作流、WebSocket、在线用户页、缓存监控页、演示 Docker 栈、历史升级脚本、多数据库兼容初始化脚本。

## Stack

| Area | Choice |
| --- | --- |
| Framework | Spring Boot |
| JDK | Java 17 |
| Database | SQL Server |
| ORM | MyBatis-Plus |
| Auth | Sa-Token |
| Cache | Redis + Redisson |
| API Docs | SpringDoc |
| Excel | FastExcel |

## Structure

```text
ruoyi-admin/       # application entry
ruoyi-common/      # retained shared capabilities
ruoyi-modules/
  carbon-vendor/   # vendor business domain
  ruoyi-system/    # retained system/RBAC/audit domain
script/sql/portal/ # current SQL Server portal seed scripts
deploy/            # current deployment compose
```

## Verification

```bash
rtk mvn -Pprod -pl ruoyi-admin -am -DskipTests compile
```
