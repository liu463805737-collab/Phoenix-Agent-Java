<div align="center">
  <img alt="Phoenix Logo" width="215" src="./apps/admin-ui/public/imgs/logo.png">
  <br>
  <h1>Phoenix Admin</h1>
</div>

**中文** | [English](./README.md)

## 简介

Phoenix Admin 是一个免费开源的多智能体（Multi-Agent）管理平台，基于 Vue 3、Vite、TypeScript 等主流技术开发，开箱即用，适用于企业级 AI 应用、个人项目及学习参考。

## 特性

- **最新技术栈**：基于 Vue 3、Vite、TypeScript 等前沿技术构建
- **TypeScript**：全量使用 TypeScript，提供完整的类型定义
- **多主题**：内置多套主题色彩，支持自定义配置
- **国际化**：完善的国际化方案，支持多语言切换
- **权限管理**：内置动态路由权限生成方案
- **多 Agent 交互**：集成 NL2SQL、智能对话等 AI 交互能力

## 预览

<div align="center">
  <img alt="Phoenix 登录页" width="100%" src="./apps/admin-ui/public/doc-imgs/login.png">
  <img alt="Phoenix 对话页" width="100%" src="./apps/admin-ui/public/doc-imgs/chat.png">
  <img alt="Phoenix 管理页" width="100%" src="./apps/admin-ui/public/doc-imgs/admin.png">
</div>

## 安装使用

1. 获取项目代码

```bash
git clone https://github.com/liu463805737-collab/phoenix.git
或
git clone https://gitee.com/lwj/phoenix.git
```

2. 安装依赖

```bash
cd web-frontend
npm i -g corepack
pnpm install
```

3. 启动开发服务

```bash
cd web-frontend/apps/admin-ui
pnpm dev
```

4. 构建

```bash
cd web-frontend/apps/admin-ui
pnpm build
```

## 如何贡献

欢迎提交 [Issue](https://codeup.aliyun.com/608787900f167f18d198f318/phoenix/issues) 或 Pull Request。

**Pull Request 流程：**

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/xxxx`
3. 提交修改：`git commit -am 'feat(function): add xxxxx'`
4. 推送分支：`git push origin feature/xxxx`
5. 提交 Pull Request

## Git 提交规范

遵循 [Angular Commit Convention](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-angular)：

- `feat` — 新功能
- `fix` — 修复 Bug
- `style` — 代码格式（不影响功能）
- `perf` — 性能优化
- `refactor` — 重构
- `revert` — 撤销修改
- `test` — 测试
- `docs` — 文档/注释
- `chore` — 依赖/配置更新
- `ci` — 持续集成
- `types` — 类型定义变更

## 浏览器支持

Tailwind CSS v4.0 支持 Safari 16.4+、Chrome 111+、Firefox 128+。

支持现代浏览器，不支持 IE。

| [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/edge/edge_48x48.png" alt="Edge" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Edge | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/firefox/firefox_48x48.png" alt="Firefox" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Firefox | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/chrome/chrome_48x48.png" alt="Chrome" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Chrome | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/safari/safari_48x48.png" alt="Safari" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Safari |
| :-: | :-: | :-: | :-: |
| last 2 versions | last 2 versions | last 2 versions | last 2 versions |

## 许可证

[MIT © Phoenix 2026](./LICENSE)
