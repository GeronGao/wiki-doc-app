# WikiDoc Android 项目规则

## 开发环境

- **Git 路径**: `D:\Program Files\Git\cmd\git.exe`
- **项目根目录**: `f:\mycode\android`

## 提交规则

**重要**: 每次完成需求后必须执行 git commit

1. 使用 `git -C f:\mycode\android` 执行所有 git 命令
2. 提交信息应包含本次修改的内容描述
3. 示例提交命令:
   ```powershell
   & "D:\Program Files\Git\cmd\git.exe" -C "f:\mycode\android" add -A
   & "D:\Program Files\Git\cmd\git.exe" -C "f:\mycode\android" commit -m "提交信息"
   ```
