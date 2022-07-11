# Useful-Code
一些常用的工具与RocketMQ轻量化客户端实现

#### RocketMQ-Client 使用
```java

@MQListener(topic="user", tags="updateUser")
public class ListenerDemo<UserDTO> implements GeneralMQListener<UserDTO> {

     public boolean consume(MessageExt msg, UserDTO body) {
         //...
         return true;
     }


}

```

#### 其他常用工具 如重试工具`Retryable.java`
```java
public static void main(String[] args) {
     //重试打印3次，间隔5s
     Retryable.of(() -> println("hello")).retry(3, 5, TimeUnit.SECOND);
}

```






