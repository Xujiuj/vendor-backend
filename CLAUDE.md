# Vendor Backend Development Contract

本目录是厂商端云端后端工程。开发时遵守上级 `AGENTS.md` 与本文件约束。

## Scope

- 默认数据库为云端 MySQL `vendor`。
- 厂商端只写厂商端业务表，不写企业端本地业务库。
- 业务代码优先复用 RuoYi 框架能力和既有项目模式。
- 保留系统基础能力：认证、RBAC、租户套餐、用户、角色、部门、岗位、公告、操作日志、登录日志、文件/Excel/缓存基础设施。
- 不恢复已剔除的 RuoYi 原生交付外能力：代码生成、任务调度、工作流、WebSocket、在线用户页、缓存监控页、演示 Docker 栈、历史升级脚本、多数据库兼容初始化脚本。

## Stack

| Area | Choice |
| --- | --- |
| Framework | Spring Boot |
| JDK | Java 17 |
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
script/sql/mysql/  # latest MySQL schema and seed scripts
deploy/            # current deployment compose
```

## Verification

```bash
rtk mvn -Pprod -pl ruoyi-admin -am -DskipTests compile
```
