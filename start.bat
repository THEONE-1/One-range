@echo off
chcp 65001 >nul
echo ========================================
echo    One 安全靶场 - 启动脚本
echo    Vue 3 + Spring Boot
echo ========================================
echo.

echo 正在启动Docker服务...
echo.

REM 检查Docker
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 启动Docker Compose
docker-compose up -d

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo ✅ One安全靶场启动成功！
    echo ========================================
    echo.
    echo 访问地址：
    echo   前端：http://localhost:3000
    echo   后端API：http://localhost:8000
    echo.
    echo 默认账号：
    echo   用户名：admin
    echo   密码：admin
    echo.
    echo 常用命令：
    echo   查看日志：docker-compose logs -f
    echo   停止服务：docker-compose down
    echo ========================================
) else (
    echo.
    echo [错误] 启动失败，请查看错误信息
)

echo.
pause
