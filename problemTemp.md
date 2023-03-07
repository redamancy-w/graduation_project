# 问题暂存

## 1.spi

模仿dubbospi,修改spi的配置文件读取方式,

不再使用dubbo原有的多文件配置,修改为yaml文件配置,

# 2. springboot中的ioc容器

在获取ioc中涉及多线程的类时,出现线程池无法实例化的问题

# 3. Nacos参数

### nacosClient 参数

![image-20230205213112580](https://cdn.jsdelivr.net/gh/redamancy-w/blogImages@main/imgimage-20230205213112580.png)



endpoint

- 增强endpoint的功能。在endpoint端配置网段和环境的映射关系，endpoint在接收到客户端的请求之后，根据客户端的来源IP所属网段，计算出该客户端所属环境，然后找到对应环境的IP列表返回给客户端。如下图

![image-20230205215615364](https://cdn.jsdelivr.net/gh/redamancy-w/blogImages@main/imgimage-20230205215615364.png)



### transient 关键字

避免被序列化

# 4. spring 生命周期的问题

> https://developer.aliyun.com/article/348588#:~:text=Instanti,法以定义实现类%E3%80%82



![image-20230228152014688](https://cdn.jsdelivr.net/gh/redamancy-w/blogImages@main/imgimage-20230228152014688.png)

![image-20230228152710051](https://cdn.jsdelivr.net/gh/redamancy-w/blogImages@main/imgimage-20230228152710051.png)

> postProcessBeforeInstantiation调用时机为bean实例化(**Instantiation**)之前 如果返回了bean实例, 则会替代原来正常通过target bean生成的bean的流程. 典型的例如aop返回proxy对象. 此时bean的执行流程将会缩短, 只会执行 