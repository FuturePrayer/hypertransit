# AGENTS.md — HyperTransit 项目开发指南

本文档面向在本仓库上工作的开发者与 AI 编程 agent，记录项目结构、构建约定、关键技术约束与易踩的坑。**修改代码前请先读这份文档。**

---

## 1. 项目总览

HyperTransit 是一个 Fabric 模组，为 Minecraft 添加高速船和矿车。单 Gradle 项目，使用 split source sets（`src/main` + `src/client`）。

```
hypertransit/
├── settings.gradle            # rootProject.name = 'hypertransit'
├── build.gradle               # Loom 配置、mods {} 块
├── gradle.properties          # 所有版本号的唯一来源
├── gradlew / gradlew.bat      # Gradle wrapper
├── gradle/wrapper/            # wrapper jar + properties
├── src/
│   ├── main/java/cn/suhoan/hypertransit/
│   │   ├── HyperTransit.java          # ModInitializer 入口
│   │   ├── entity/
│   │   │   ├── ModBoatEntity.java     # 自定义船实体（speedMultiplier）
│   │   │   ├── ModMinecartEntity.java # 自定义矿车实体（speedMultiplier + 防脱轨）
│   │   │   └── ModEntityTypes.java    # 实体类型注册
│   │   ├── item/
│   │   │   └── ModItems.java          # 物品注册 + 创造模式物品栏
│   │   └── mixin/
│   │       └── AbstractBoatMixin.java # 船速度 Mixin
│   ├── main/resources/
│   │   ├── fabric.mod.json
│   │   ├── hypertransit.mixins.json
│   │   ├── assets/hypertransit/       # 纹理、模型、语言文件、物品 JSON
│   │   └── data/hypertransit/recipe/  # 合成配方
│   └── client/java/cn/suhoan/hypertransit/client/
│       ├── HyperTransitClient.java    # ClientModInitializer
│       └── renderer/
│           ├── ModBoatRenderer.java
│           └── ModMinecartRenderer.java
├── README.md
├── AGENTS.md                  # 本文档
├── LICENSE                    # Apache 2.0
└── logo.png
```

### 技术栈版本

| 组件 | 版本 |
|------|------|
| Minecraft | **26.2**（新版本号方案，Mojang 官方 mappings） |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.155.2+26.2 |
| Fabric Loom | 1.17-SNAPSHOT |
| Java | **25** |

---

## 2. 构建与运行

```bash
# 构建（产物：build/libs/hypertransit-<版本>.jar）
./gradlew build

# 生成 MC 反编译源码（查阅 Mojang mappings 类名）
./gradlew genSources

# 启动开发客户端
./gradlew runClient
```

- **版本号唯一来源是 `gradle.properties`**：`minecraft_version` / `loader_version` / `fabric_api_version` / `loom_version` / `mod_version`。改版本只改这一处。
- **不要引入 Yarn mappings**：MC 26.1+ 官方发布去混淆代码，Loom 1.17 默认使用 Mojang 官方 mappings。`mappings "net.fabricmc:yarn:..."` 配置在 Loom 1.17 中不存在。

---

## 3. 核心机制

### 3.1 船速度控制（Mixin）

`AbstractBoatMixin` 注入 `AbstractBoat.controlBoat()` 方法：
- **HEAD 注入**：保存进入 `controlBoat()` 前的 `deltaMovement`
- **TAIL 注入**：计算 `controlBoat()` 添加的速度增量，将水平分量乘以 `speedMultiplier`

倍率存储在 `ModBoatEntity` 的 `SynchedEntityData`（`DATA_SPEED_MULTIPLIER`），客户端/服务端同步。

### 3.2 矿车速度控制

`ModMinecartEntity` 重写 `getMaxSpeed(ServerLevel)`：
```java
return super.getMaxSpeed(level) * this.getSpeedMultiplier();
```
- 新行为（实验性"矿车改进"）：`getMaxSpeed` 委托给 `MinecartBehavior`，读取 `GameRules.MAX_MINECART_SPEED`
- 旧行为：硬编码 0.4（陆地）/ 0.2（水中）

### 3.3 矿车防脱轨（move 重写）

`ModMinecartEntity.move()` 在铁轨上且 `MoverType.SELF` 时：
1. 将大位移拆分为每步 ≤0.4 格的子步
2. 每步调用 `super.move()`（保留碰撞检测）
3. 每步后调用 `snapToRailCenter()` 将位置修正到轨道中心线

`snapToRailCenter()` 按轨道形状修正：
- 直线轨道：将横向轴吸附到方块中心
- 弯道：将位置投影到弯道对角线（保持沿轨道方向的进度不变）

---

## 4. 关键约束

### MC 26.2 API 注意事项

| 要点 | 说明 |
|------|------|
| 物品注册 | `Item.Properties` 必须先调用 `.setId(ResourceKey<Item>)` 再构造 Item |
| 实体注册 | `EntityType.Builder.of()` + `ResourceKey.create()` + `builder.build(key)` + `Registry.register()` |
| 创造模式物品栏 | `net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent()` |
| Client Item JSON | MC 26.2 要求 `assets/<modid>/items/<name>.json` 引用物品模型 |
| 标识符 | `Identifier.fromNamespaceAndPath(ns, path)`（非 `Identifier.of`） |

### 矿车双行为系统

MC 26.2 有两套矿车运动逻辑：
- **NewMinecartBehavior**：实验性"矿车改进"开启时使用，有 `TrackIteration` 循环逐方块跟踪
- **OldMinecartBehavior**：普通存档使用，每 tick 只处理一个方块

修改矿车行为时必须同时考虑两种行为路径。

---

## 5. 代码约定

- **包名**：`cn.suhoan.hypertransit.*`
- **Mod ID**：`hypertransit`
- **日志**：`HyperTransit.LOGGER`（SLF4J）
- **客户端代码**必须在 `src/client/java`（split source sets）
- **Mixin 命名**：`hypertransit$` 前缀用于 `@Unique` 字段/方法

---

## 6. 修改检查清单

改动后逐项确认：
- [ ] 用了 MC 类名 → 确认是 MC 26.2 Mojang 名（`genSources` 核实）
- [ ] 新增物品 → `Item.Properties` 必须 `.setId(key)`；添加 `assets/hypertransit/items/<name>.json`
- [ ] 新增实体 → 注册 `EntityType` + `Item` + 渲染器 + 纹理 + 语言文件
- [ ] 修改矿车运动逻辑 → 同时测试新行为（实验性特性）和旧行为（普通存档）
- [ ] 修改速度倍率 → 同步更新 `ModEntityTypes` 的注册常量和 `getBoatSpeedMultiplier`/`getMinecartSpeedMultiplier`
- [ ] 升级 mod 版本 → 同步更新 `README.md` 的版本对照表
- [ ] `./gradlew build` 通过
