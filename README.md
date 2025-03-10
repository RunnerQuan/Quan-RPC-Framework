# Quan-RPC

Quan-RPC 是一款使用 Java 实现的基于TCP协议的远程过程调用（RPC）框架，用于在分布式系统中实现服务的远程调用。它通过自定义的协议、借助自定义的服务注册中心在客户端和服务器之间传递数据，确保可靠的通信和高效的服务调用。

## 整体架构图

<img src="docs/assets/image-20250305174957086.png" alt="image-20250305174957086" style="zoom:50%;" />

## 项目特点：
- 使用Java原生Socket API（同步阻塞IO模型）
- 自定义实现的注册中心
- 自定义消息格式
![image-20240712222852778](docs/Development%20Documentation.assets/image-20240712222852778.png)
- 基于心跳机制的服务健康检查

## 开发环境：

操作系统：Windows 11

编程语言：Java

软件开发工具包：JDK11（本项目使用Amazon Corretto 11）

集成开发环境（IDE）：IntelliJ IDEA 2023.2.1

项目管理工具：Maven

容器化工具：Docker、Docker Compose

版本控制：Git

## 项目模块：

docs ------ 存储项目相关文档

out/artifacts ------ 存放编译后打包的文件

rpc-api ------ 服务端与客户端的公共调用接口

rpc-common ------ 实体对象、公共工具类、枚举类、自定义异常类

rpc-core ------ Quan-RPC 框架的核心实现

test-client ------ 客户端测试代码

test-server ------ 服务端测试代码

## 项目运行教程：

启动命令：

服务注册中心：java -jar registry.jar

服务端：java -jar server.jar -l 192.168.1.11 -p 15555

客户端：java -jar client.jar -i 192.168.1.16 -p 12375

![image-20240712222826115](docs/Development%20Documentation.assets/image-20240712222826115.png)
