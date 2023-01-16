package fang.redamancy.core.common.model;

import lombok.Data;

import java.util.Map;

/**
 * @Author redamancy
 * @Date 2023/1/9 21:07
 * @Version 1.0
 */

@Data
public class ExtensionYaml {
    private Map<String, Map<String, String>> extensionClasses;

    /**
     * 返回该接口下的所有类
     *
     * @param className 接口名
     */
    public Map<String, String> getClassesMap(String className) {
        Map<String, String> extension = extensionClasses.get(className);
        
        if (extension != null && extension.size() > 0) {
            return extension;
        } else {
            return null;
        }
    }
}
