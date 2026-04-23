@echo off
echo ========================================
echo 合并前端项目
echo ========================================

echo.
echo [1/5] 打包 mc-user-web (用户端)...
cd mc-user-web
call npm run build
if errorlevel 1 (
    echo 错误: mc-user-web 打包失败！
    pause
    exit /b 1
)

echo.
echo [2/5] 打包 ruoyi-ui (管理后台)...
cd ..\ruoyi-ui
call npm run build:prod
if errorlevel 1 (
    echo 错误: ruoyi-ui 打包失败！
    pause
    exit /b 1
)

echo.
echo [3/5] 创建合并目录...
cd ..
if exist "dist" rmdir /s /q dist
mkdir dist

echo.
echo [4/5] 复制 mc-user-web 到 dist...
xcopy /s /e /i /y "mc-user-web\dist\*" "dist\"

echo.
echo [5/5] 复制 ruoyi-ui 到 dist\admin...
mkdir "dist\admin"
xcopy /s /e /i /y "ruoyi-ui\dist-admin\*" "dist\admin\"

echo.
echo ========================================
echo 合并完成！
echo ========================================
echo.
echo 访问地址:
echo   用户端: http://localhost/
echo   管理端: http://localhost/admin
echo.
echo 部署说明:
echo   将 dist 目录部署到 Web 服务器即可
echo.
pause
