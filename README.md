# WikiDoc

[English](#english) | [中文](#中文)

---

## English

### Introduction

WikiDoc is a powerful local Wiki document management application for Android. It provides complete Markdown support, efficient preview capabilities, and batch import/export functionality.

### Features

- **Full Markdown Support**: Complete GFM (GitHub Flavored Markdown) syntax support including tables, task lists, code blocks, and more
- **Multiple Preview Modes**:
  - Edit Mode: Focus on writing
  - Preview Mode: Read-only rendered view
  - Split Mode: Edit and preview side by side
- **Organization**:
  - Folder-based organization with tree structure
  - Favorites system
  - Full-text search
- **Image Management**:
  - Built-in image picker
  - Image gallery with usage tracking
  - Automatic image compression
- **Import/Export**:
  - Import from SMB/CIFS servers
  - Export to local storage
  - Batch operations support
- **Additional Features**:
  - Mermaid diagram support
  - Code syntax highlighting
  - Word count statistics
  - Auto-save

### Screenshots

> TODO: Add screenshots here

### Installation

#### From Source

1. Clone the repository:
```bash
git clone https://github.com/yourusername/wikidoc.git
cd wikidoc
```

2. Build debug APK:
```bash
./gradlew assembleDebug
```

3. Install on connected device/emulator:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Requirements

- Android SDK 34 (Android 14) or higher
- Kotlin 1.9+
- Gradle 8.4+

### Architecture

The app follows **MVVM + Clean Architecture** pattern:

```
├── presentation/     # UI Layer (Compose UI, ViewModels)
├── domain/          # Business Logic (UseCases, Entities)
├── data/            # Data Layer (Room DB, Repositories)
└── core/            # Common utilities
```

### Tech Stack

| Category | Technology |
|----------|------------|
| UI Framework | Jetpack Compose |
| DI | Hilt |
| Database | Room |
| Navigation | Compose Navigation |
| Markdown | commonmark-java |
| Image Loading | Coil |
| SMB Client | jcifs-ng |

### Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 中文

### 介绍

WikiDoc 是一款功能强大的本地 Wiki 文档管理应用，支持完整的 Markdown 语法、高效的预览功能以及批量导入导出。

### 功能特点

- **完整 Markdown 支持**：支持 GitHub 风格的 Markdown (GFM) 语法，包括表格、任务列表、代码块等
- **多种预览模式**：
  - 编辑模式：专注写作
  - 预览模式：仅查看渲染后的效果
  - 分屏模式：编辑和预览并排显示
- **文档组织**：
  - 基于文件夹的树形结构管理
  - 收藏功能
  - 全文搜索
- **图片管理**：
  - 内置图片选择器
  - 图片库管理，显示使用情况
  - 自动图片压缩
- **导入导出**：
  - 从 SMB/CIFS 服务器导入
  - 导出到本地存储
  - 批量操作支持
- **其他功能**：
  - Mermaid 流程图支持
  - 代码语法高亮
  - 字数统计
  - 自动保存

### 截图

> TODO: 添加截图

### 安装

#### 从源码编译

1. 克隆仓库：
```bash
git clone https://github.com/yourusername/wikidoc.git
cd wikidoc
```

2. 编译 Debug 版本：
```bash
./gradlew assembleDebug
```

3. 安装到设备或模拟器：
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### 系统要求

- Android SDK 34 (Android 14) 或更高版本
- Kotlin 1.9+
- Gradle 8.4+

### 架构设计

应用采用 **MVVM + 清洁架构** 模式：

```
├── presentation/     # 表现层 (Compose UI, ViewModels)
├── domain/           # 领域层 (用例, 实体)
├── data/            # 数据层 (Room 数据库, 仓库实现)
└── core/            # 公共工具类
```

### 技术栈

| 类别 | 技术 |
|------|------|
| UI 框架 | Jetpack Compose |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 导航 | Compose Navigation |
| Markdown 解析 | commonmark-java |
| 图片加载 | Coil |
| SMB 客户端 | jcifs-ng |

### TODO

#### 核心功能
- [ ] 云同步支持 (WebDAV/Google Drive)
- [ ] 文档版本历史
- [ ] 标签系统优化
- [ ] PDF 导出功能

#### 用户体验
- [ ] 深色模式完善
- [ ] 夜间模式定时开关
- [ ] 主题自定义
- [ ] 编辑器字体大小调节
- [ ] 快捷键支持 (外接键盘)

#### 数据管理
- [ ] 文档模板系统
- [ ] 批量重命名
- [ ] 文档合并功能
- [ ] 数据备份与恢复

#### 跨平台
- [ ] Kotlin Multiplatform 支持
- [ ] 桌面端应用 (Compose Multiplatform)
- [ ] Web 版本

#### 其他
- [ ] 国际化 (i18n)
- [ ] 性能优化
- [ ] 单元测试和 UI 测试
- [ ] 文档完善

### 贡献

欢迎提交 Pull Request！如果您有任何问题或建议，请提交 Issue。

### 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件。

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=yourusername/wikidoc&type=Date)](https://star-history.com/#yourusername/wikidoc&Date)
