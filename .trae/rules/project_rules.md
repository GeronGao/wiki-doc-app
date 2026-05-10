# WikiDoc Android 项目规则

## 开发环境

- **Git 路径**: `D:\Program Files\Git\cmd\git.exe`
- **项目根目录**: `f:\mycode\android`

## 编译方式

**重要**: 由于没有签名密钥，所有编译都使用 debug 模式

1. **Debug 编译** (用于日常开发和测试):
   ```powershell
   .\gradlew.bat assembleDebug
   ```

2. **清理并重新编译**:
   ```powershell
   .\gradlew.bat clean assembleDebug
   ```

3. **安装到设备**:
   ```powershell
   adb install -r "f:\mycode\android\app\build\outputs\apk\debug\app-debug.apk"
   ```

4. **一键编译并安装**:
   ```powershell
   .\gradlew.bat assembleDebug; adb install -r "f:\mycode\android\app\build\outputs\apk\debug\app-debug.apk"
   ```

5. **Release 编译** (需要签名密钥，未配置):
   ```powershell
   .\gradlew.bat assembleRelease
   ```

## 提交规则

**重要**: 每次完成需求后必须执行 git commit

1. 使用 `git -C f:\mycode\android` 执行所有 git 命令
2. 提交信息应包含本次修改的内容描述
3. 示例提交命令:
   ```powershell
   & "D:\Program Files\Git\cmd\git.exe" -C "f:\mycode\android" add -A
   & "D:\Program Files\Git\cmd\git.exe" -C "f:\mycode\android" commit -m "提交信息"
   ```
