# community
2021年1月28日23:21:22 项目的第一次提交 开发完成首页

2021年1月29日21:39:29
添加功能:

1.开发注册发送邮件功能
• 邮箱设置
- 启用客户端SMTP服务
• Spring Email
- 导入 jar 包
- 邮箱参数配置
- 使用 JavaMailSender 发送邮件
• 模板引擎
- 使用 Thymeleaf 发送 HTML 邮件

2.开发注册功能
• 访问注册页面
- 点击顶部区域内的链接，打开注册页面。
• 提交注册数据
- 通过表单提交数据。
- 服务端验证账号是否已存在、邮箱是否已注册。
- 服务端发送激活邮件。
• 激活注册账号
- 点击邮件中的链接，访问服务端的激活服务。

3.生成登录页验证码
• Kaptcha
- 导入 jar 包
- 编写 Kaptcha 配置类
- 生成随机字符、生成图片

2021年1月31日22:00:18
添加功能:
1. 开发登录、退出功能
• 访问登录页面
- 点击顶部区域内的链接，打开登录页面。
• 登录
- 验证账号、密码、验证码。
- 成功时，生成登录凭证，发放给客户端。
- 失败时，跳转回登录页。
• 退出
- 将登录凭证修改为失效状态。
- 跳转至网站首页。
2. 显示登录信息
• 拦截器示例
- 定义拦截器，实现HandlerInterceptor
- 配置拦截器，为它指定拦截、排除的路径
• 拦截器应用
- 在请求开始时查询登录用户
- 在本次请求中持有用户数据
- 在模板视图上显示用户数据
- 在请求结束时清理用户数据
3. 账号设置
• 上传文件
- 请求：必须是POST请求
- 表单：enctype=“multipart/form-data”
- Spring MVC：通过 MultipartFile 处理上传文件
• 开发步骤
- 访问账号设置页面
- 上传头像
- 获取头像
4. 检查登录状态
• 使用拦截器
- 在方法前标注自定义注解
- 拦截所有请求，只处理带有该注解的方法
• 自定义注解
- 常用的元注解：
@Target、@Retention、@Document、@Inherited
- 如何读取注解：
Method.getDeclaredAnnotations​()
Method.getAnnotation​(Class<T> annotationClass)
5. 过滤敏感词
• 前缀树
- 名称：Trie、字典树、查找树
- 特点：查找效率高，消耗内存大
- 应用：字符串检索、词频统计、字符串排序等
• 敏感词过滤器
- 定义前缀树
- 根据敏感词，初始化前缀树
- 编写过滤敏感词的方法
6. 发布帖子
• AJAX
- Asynchronous JavaScript and XML
- 异步的JavaScript与XML，不是一门新技术，只是一个新的术语。
- 使用AJAX，网页能够将增量更新呈现在页面上，而不需要刷新整个页面。
- 虽然X代表XML，但目前JSON的使用比XML更加普遍。
- https://developer.mozilla.org/zh-CN/docs/Web/Guide/AJAX
• 示例
- 使用jQuery发送AJAX请求。
• 实践
- 采用AJAX请求，实现发布帖子的功能。
7. 帖子详情
• DiscussPostMapper
• DiscussPostService
• DiscussPostController
• index.html
- 在帖子标题上增加访问详情页面的链接
• discuss-detail.html
