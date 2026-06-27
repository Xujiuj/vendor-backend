# Vendor Backend

厂商端云端后端服务，基于 RuoYi-Vue-Plus 的认证、权限、审计、租户和通用工程能力，承载客户档案、套餐授权、因子版本、模板分发、续费订单与开放接口。

## Delivery Boundary

- 运行形态：独立 Spring Boot/Maven 后端，默认连接云端 MySQL `vendor`。
- 数据边界：只写入厂商端业务表，不写企业端本地业务库。
- 保留能力：登录认证、RBAC、用户/角色/部门/岗位、租户套餐、公告、操作日志、登录日志、文件/Excel/缓存基础能力。
- 业务能力：客户档案、License 授权、因子维表、因子版本、因子开放范围、模板库、模板分发、续费订单、企业端开放 API。
- 已剔除能力：RuoYi 代码生成、在线用户页、缓存监控页、SnailJob 任务调度、Warm-Flow 工作流、WebSocket 推送、原始演示 Docker 栈、多数据库兼容初始化脚本、历史升级脚本。

## Modules

```text
vendor-backend/
  ruoyi-admin/       # application entry and HTTP controllers
  ruoyi-common/      # shared framework capabilities retained by vendor delivery
  ruoyi-modules/
    carbon-vendor/   # vendor business domain
    ruoyi-system/    # retained system/RBAC/audit domain
  script/sql/mysql/  # latest MySQL schema and seed scripts
  deploy/            # current cloud deployment compose file
```

## Verification

```bash
mvn -Pprod -pl ruoyi-admin -am -DskipTests compile
```

All shell commands in this workspace should be run through `rtk`, for example:

```bash
rtk mvn -Pprod -pl ruoyi-admin -am -DskipTests compile
```
