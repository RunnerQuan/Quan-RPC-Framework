# Quan-RPC-Framework 开发文档
## 1. 项目背景
这是一个计算机网络实验课程的大作业。

## 2. 项目简介
这是一个基于RPC（远程过程调用）的分布式计算框架，支持多种语言的客户端和服务端。

## 3. 项目结构
- `docs`: 项目文档
- `rpc-api`：服务端与客户端的公共调用接口
- `rpc-common`：服务端与客户端的公共工具类和枚举类
- `rpc-core`：RPC框架的核心实现
- `test-client`：客户端测试代码
- `test-server`：服务端测试代码

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
通用接口 `HelloService`
```java
public interface HelloService {
    String hello(HelloObject object);
}
```
hello方法参数为 `HelloObject` 对象，定义如下：
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
- 传输格式：
  - 待调用接口名称
  - 待调用方法名称
  - 调用方法的参数
  - 调用方法的参数类型
- 定义传输协议的接口：
  - `RpcRequest`：封装请求的信息
    ```java
    @Data
    @Builder
    public class RpcRequest implements Serializable {
    /**
    * 待调用接口名称
    */
    private String interfaceName;
    
        /**
         * 待调用方法名称
         */
        private String methodName;
    
        /**
         * 调用方法的参数
         */
        private Object[] parameters;
    
        /**
         * 调用方法的参数类型
         */
        private Class<?>[] paramTypes;
    }
    ```
  - `RpcResponse`：封装响应的信息
    ```java
    @Data
    @Builder
    public class RpcResponse implements Serializable {
        /**
         * 响应状态码
         */
        private Integer statusCode;
    
        /**
         * 响应状态信息
         */
        private String message;
    
        /**
         * 响应数据
         */
        private Object data;
    
        /**
        * 成功
        */
        public static <T> RpcResponse<T> success(T data) {
            RpcResponse<T> response = new RpcResponse<>();
            response.setStatusCode(ResponseCode.SUCCESS.getCode());
            response.setData(data);
            return response;
        }

        /**
        * 失败
        */
        public static <T> RpcResponse<T> fail(ResponseCode code) {
           RpcResponse<T> response = new RpcResponse<>();
            response.setStatusCode(code.getCode());
            response.setMessage(code.getMessage());
            return response;
        }
    }
    ```
    
---
#### 客户端实现——动态代理
- 由于客户端无法直接调用服务端的方法，因此需要通过动态代理的方式生成实例，并且调用方法时需要生成RpcRequest对象并发送给服务端。
- 采用JDK动态代理，代理类需要实现`InvocationHandler`接口
- 传递host和port来指明服务端的位置
- `InvocationHandler`接口需要实现`invoke`方法，来指明代理对象的方法被调用时的动作：
  - 生成一个`RpcRequest`对象，发送出去，然后返回从服务端接受到的RpcResponse对象
```java
    public class RpcClientProxy implements InvocationHandler {
        private static final Logger logger = Logger.getLogger(RpcClientProxy.class.getName());
        private String host;
        private int port;
      
        /**
         * 构造函数
         * @param host
         * @param port
         */
        public RpcClientProxy(String host, int port) {
            this.host = host;
            this.port = port;
        }
      
        /**
         * 获取代理对象
         * @param clazz
         * @param <T>
         * @return
         */
        @SuppressWarnings("unchecked")
        public <T> T getProxy(Class<T> clazz) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
        }
      
        /**
         * 代理对象调用方法时的操作
         * @param proxy
         * @param method
         * @param args
         * @return
         * @throws Throwable
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            logger.info("调用方法: " + method.getName());
            RpcRequest rpcRequest = RpcRequest.builder()
                    .interfaceName(method.getDeclaringClass().getName())
                    .methodName(method.getName())
                    .parameters(args)
                    .paramTypes(method.getParameterTypes())
                    .build();
            RpcClient rpcClient = new RpcClient();
            return rpcClient.sendRequest(rpcRequest, host, port);
        }
    }
```
 - `RpcClient`类用于发送`RpcRequest`对象，并接受`RpcResponse`对象
 - `sendRequest`方法：
   - 通过`Socket`连接服务端
   - 将`RpcRequest`对象序列化后发送给服务端
   - 从服务端接受`RpcResponse`对象
   - 关闭`Socket`连接
   - 返回`RpcResponse`对象
```java
public class RpcClient {

    /**
     * 日志记录
     */
    private static final Logger logger = Logger.getLogger(RpcClient.class.getName());

    /**
     * 发送请求到服务端并获取结果
     * @param rpcRequest 请求对象
     * @param host 服务端主机地址
     * @param port 服务端端口
     * @return 服务端返回的结果
     */
    public Object sendRequest(RpcRequest rpcRequest,  String host, int port) {
        try (Socket socket = new Socket(host, port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
            if(rpcResponse == null) {
                logger.severe("服务调用失败，service：" + rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.severe("服务调用失败，service：" + rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("调用时发生错误：" + e);
            return null;
        }
    }
}
```
---

#### 服务端实现——反射调用
- 服务端的实现使用一个`ServerSocket`监听某个端口，循环接收连接请求，如果发来了一个请求就创建一个线程，在新的线程中处理调用。
- 创建线程采用线程池：
- 目前`RpcServer`只能注册一个接口，即对外提供一个接口的调用服务，添加`register`方法用于注册服务，在注册完服务后立刻开始监听
```java
public class RpcServer {
    /**
     * 用于处理请求的线程池
     */
    private final ExecutorService threadPool;

    /**
     * 用于记录与 RpcServer 相关的日志
     */
    private static final Logger logger = Logger.getLogger(RpcServer.class.getName());

    /**
     * 构造函数
     */
    public RpcServer() {
        int corePoolSize = 5; // 核心线程数5
        int maximumPoolSize = 50; // 最大线程数50
        long keepAliveTime = 60; // 线程存活时间60s
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    /**
     * 用于注册服务
     */
    public void register(Object service, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器正在启动...");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接！ IP为：" + socket.getInetAddress() + " 端口号为:" + socket.getPort());
                threadPool.execute(new RequestHandler(socket, service));
            }
        } catch (IOException e) {
            logger.severe("连接时有错误发生：" + e);
        }
    }
}
```
- `RequestHandler`类用于处理请求，实现`Runnable`接口
- `run`方法：
  - 从`Socket`中获取`RpcRequest`对象
  - 通过反射调用服务端的方法
  - 将结果封装成`RpcResponse`对象并发送给客户端
  - 捕获异常并记录日志
- `invokeMethod`方法：
  - 通过反射调用服务端的方法
  - 如果接口不存在，返回`RpcResponse.fail(ResponseCode.CLASS_NOT_FOUND)`
  - 如果方法不存在，返回`RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND)`
```java
public class RequestHandler implements Runnable {
    /**
     * 用于记录与 RequestHandler 相关的日志
     */
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    /**
     * 用于处理客户端的请求并将处理结果返回给客户端
     */
    private Socket socket;

    /**
     * 服务对象
     */
    private Object service;

    /**
     * 构造函数
     */
    public RequestHandler(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    /**
     * 用于处理客户端的请求并将处理结果返回给客户端
     */
    @Override
    public void run() {
        // 使用try-with-resources确保操作完成后socket能够正常关闭
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object returnObject = invokeMethod(rpcRequest); // 反射调用本地服务
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            logger.severe("调用或发送时有错误发生：" + e);
        }
    }

    /**
     * 反射调用本地服务
     * @param rpcRequest
     * @return
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private Object invokeMethod(RpcRequest rpcRequest) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Class<?> clazz = Class.forName(rpcRequest.getInterfaceName());
        if(!clazz.isAssignableFrom((service.getClass()))) {
            return RpcResponse.fail(ResponseCode.CLASS_NOT_FOUND);
        }
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
```
---
#### 测试
- 服务端：
```java
public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImplementation();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9000);
    }
}
```
- 客户端：通过动态代理，生成代理对象，并调用
```java
public class TestClient {
    public static void main(String[] args) {
         RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
         HelloService helloService = proxy.getProxy(HelloService.class);
         HelloObject object = new HelloObject(12, "This is a message");
         String res = helloService.hello(object);
         System.out.println(res);
    }
}
```
- 首先启动服务端，开始监听；再启动客户端
- 服务端输出：
```
6月 11, 2024 11:16:05 上午 com.quan.server.RpcServer register
信息: 服务器正在启动...
6月 11, 2024 11:16:10 上午 com.quan.server.RpcServer register
信息: 客户端连接！ IP为：/127.0.0.1 端口号为:59245
6月 11, 2024 11:16:10 上午 com.quan.HelloServiceImplementation hello
信息: 接收到：This is a message
```
- 客户端输出：
```
6月 11, 2024 11:16:10 上午 com.quan.client.RpcClientProxy invoke
信息: 调用方法: hello
这是调用的返回值， id=618
```