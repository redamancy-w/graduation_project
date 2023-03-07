package fang.redamancy.core.register.api.registration;

import fang.redamancy.core.common.net.support.Node;

/**
 * 注册中心的客户端，
 * 继承Node接口
 * - 查看注册中心是否活跃，
 * - 查看注册中心的ip，port等信息
 * 继承ServiceRegistry
 * - 注册服务
 * - 注销服务
 *
 * @Author redamancy
 * @Date 2023/2/4 18:53
 * @Version 1.0
 */
public interface Register extends ServiceRegistry, Node {
    
}
