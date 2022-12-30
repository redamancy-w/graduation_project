# The End of College

## 搭建架构

### 11.10 技术选型以及项目的结构初步搭建

![image-20221111102624145](https://cdn.jsdelivr.net/gh/redamancy-w/blogImages@main/imgimage-20221111102624145.png)V

### 11.11 

netty部分代码的编写和测试

![image-20221111102717159](https://cdn.jsdelivr.net/gh/redamancy-w/blogImages@main/imgimage-20221111102717159.png)



- 无明显问题

### 12.30

1.序列化框架的使用出现问题, 

> 在多线程下,kryo的对象池无法正常获取和使用

2.具体造成原因

> 对象池技术是所有并发安全方案中性能最好的，只要对象池大小评估得当，就能在占用极小内存空间的情况下完美解决并发安全问题。这也是 PowerJob 诞生初期使用的方案，直到...PowerJob 正式推出容器功能后，才不得不放弃该完美方案。在容器模式下，使用 kryo 对象池计算会有什么问题呢？这里简单给大家提一下PowerJob 容器功能指的是动态加载外部代码进行执行，为了进行隔离，PowerJob 会使用单独的类加载器完成容器中类的加载。因此，每一个 powerjob-worker 中存在着多个类加载器，分别是系统类加载器（负责项目的加载）和每个容器自己的类加载器（加载容器类）。序列化工具类自然是 powerjob-worker 的一部分，随 powerjob-worker 的启动而被创建。当 kryo 对象池被创建时，其使用的类加载器是系统类加载器。因此，当需要序列化/反序列化容器中的类时，kryo 并不能从自己的类加载器中获取相关的类信息，妥妥的抛出 ClassNotFoundError！
>
> 因此，PowerJob 在引入容器技术后，只能退而求其次，采取了第二种并发安全方法：ThreadLocal。

3.最后解决办法:

> 使用ThreadLocal

