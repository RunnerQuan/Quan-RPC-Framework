# Quan-RPC-Framework 开发文档
## 1. 项目背景
这是一个计算机网络实验课程的大作业。

## 2. 项目简介
这是一个基于RPC（远程过程调用）的分布式计算框架，支持多种语言的客户端和服务端。

## 3. 项目结构
- docs: 项目文档
- rpc-api：服务端与客户端的公共调用接口
- rpc-common：服务端与客户端的公共工具类和枚举类
- rpc-core：RPC框架的核心实现
- test-client：客户端测试代码
- test-server：服务端测试代码

## 4. 开发环境
- Java 11
- JetBrains Intellij IDEA 2023.2.1

## 5. 项目启动

## 6. 项目测试

## 7. 项目部署

## 8. 项目总结

## 9. 项目展望

## 开发过程：
### 基础 RPC 框架的实现 [v1.0]
#### 通用接口 rpc-api 的实现
通用接口 HelloService
```java
public interface HelloService {
    String hello(HelloObject object);
}
```
hello方法参数为 HelloObject 对象，定义如下：
```java
@Data
@AllArgsConstructor
public class HelloObject implements Serializable {
    private Integer id;
    private String message;
}

/**
 * @Data和@AllArgsConstructor是lombok插件提供的注解，用于简化Java代码
 * @Data：自动生成getter、setter、toString、equals、hashCode等方法
 * @AllArgsConstructor：自动生成全参构造方法
 */
```
---

#### 传输协议

---

#### 客户端实现——动态代理

---

#### 服务端实现——反射调用