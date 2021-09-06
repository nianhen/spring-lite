package com.nianhen.springlite;

import com.nianhen.springlite.annotation.Autowired;
import com.nianhen.springlite.annotation.Component;
import com.nianhen.springlite.annotation.ComponentScan;
import com.nianhen.springlite.annotation.Scope;
import com.nianhen.springlite.inter.BeanNameAware;
import com.nianhen.springlite.inter.BeanPostProcessor;
import com.nianhen.springlite.inter.InitializingBean;
import com.nianhen.springlite.po.BeanDefinition;
import com.nianhen.springlite.po.ScopeEnum;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @description: SpringLite容器
 * @author: nianhen
 * @since: 2021-09-03 22:27
 **/
public class SpringLiteApplicationContext {

    private static final String DEFAULT_COMPONENT_VALUE = "";

    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * 容器构造函数
     *
     * @param configClass 配置类
     */
    public SpringLiteApplicationContext(Class<?> configClass) {
        System.out.println("-------------SpringLiteApplicationContext init Begin-------------");
        scan(configClass);
        instanceSingletonBean();
        System.out.println("-------------SpringLiteApplicationContext init Finished-------------");
    }

    /**
     * 扫描包，生成Bean定义放入缓存池
     *
     * @param configClass 配置类
     */
    private void scan(Class<?> configClass) {
        String packagePath = "";
        // 获取需要扫描的路径
        boolean annotationPresent = configClass.isAnnotationPresent(ComponentScan.class);
        if (!annotationPresent) {
            System.out.println("-------------No path is configured for component scan, exit-------------");
            return;
        }
        ComponentScan componentScanAnnotation = configClass.getAnnotation(ComponentScan.class);
        packagePath = componentScanAnnotation.value();
        System.out.println("-------------Path for component scan:" + packagePath + "-------------");
        // 获取路径下的所有类
        List<Class<?>> classList = getBeanClasses(packagePath);
        classList.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .forEach(clazz -> {
                    // 遍历后置处理器
                    collectPostProcessor(clazz);
                    // 生成Bean定义
                    createBeanDefinition(clazz);
                });
    }

    /**
     * 获取后置处理器集合
     *
     * @param clazz 类
     */
    private void collectPostProcessor(Class<?> clazz) {
        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
            try {
                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                beanPostProcessorList.add(instance);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实例化单例Bean，放入缓存池
     */
    private void instanceSingletonBean() {
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (ScopeEnum.singleton.equals(beanDefinition.getScope())) {
                Object bean = doCreateBean(beanName, beanDefinition);
                if (null != bean) {
                    singletonObjects.put(beanName, bean);
                }
            }
        });
    }

    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClazz = beanDefinition.getBeanClass();
        try {
            // 实例化Bean对象
            Constructor<?> declaredConstructor = beanClazz.getDeclaredConstructor();
            Object instance = declaredConstructor.newInstance();

            // 填充属性
            Field[] fields = beanClazz.getDeclaredFields();
            // 属性上有Autowired，需要获取对应Bean赋值
            Object finalInstance = instance;
            Arrays.stream(fields).filter(field -> field.isAnnotationPresent(Autowired.class))
                    .forEach(field -> {
                        String fieldName = field.getName();
                        Object bean = getBean(fieldName);
                        field.setAccessible(true);
                        try {
                            field.set(finalInstance, bean);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

            // BeanNameAware回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 执行后置处理器预初始化逻辑
            if (!(instance instanceof BeanPostProcessor)) {
                for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                    instance = beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
                }
            }

            // 实现了初始化接口，需要先执行 afterPropertiesSet 方法
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // 执行后置处理器初始化后逻辑
            if (!(instance instanceof BeanPostProcessor)) {
                for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                    instance = beanPostProcessor.postProcessAfterInitialization(beanName, instance);
                }
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        System.out.println("-------------Failed to create bean: " + beanName + "-------------");
        return null;
    }

    /**
     * 从单例池获取Bean对象，池中没有则创建
     *
     * @param beanName Bean名称
     * @return Bean对象
     */
    public Object getBean(String beanName) {
        if (null == beanName) {
            return null;
        }
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        } else {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (null == beanDefinition) {
                System.out.println("-------------Failed to find bean and beanDefinition: " + beanName + "-------------");
                return null;
            }
            return doCreateBean(beanName, beanDefinition);
        }
    }

    /**
     * 根据类创建Bean定义
     *
     * @param clazz 类
     */
    private void createBeanDefinition(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(clazz);

        // 获取Bean名称
        String beanName = clazz.getAnnotation(Component.class).value();
        if (DEFAULT_COMPONENT_VALUE.equals(beanName)) {
            // 未设置名称，生成默认BeanName
            beanName = Introspector.decapitalize(clazz.getSimpleName());
        }
        beanDefinition.setBeanName(beanName);

        // 解析Scope
        ScopeEnum scope = ScopeEnum.singleton;
        if (clazz.isAnnotationPresent(Scope.class)) {
            String scopeValue = clazz.getAnnotation(Scope.class).value();
            if (ScopeEnum.prototype.name().equals(scopeValue)) {
                scope = ScopeEnum.prototype;
            }
        }
        beanDefinition.setScope(scope);
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    /**
     * 获取路径下的所有类
     *
     * @param packagePath 路径
     * @return 类列表
     */
    private List<Class<?>> getBeanClasses(String packagePath) {

        List<Class<?>> classList = new ArrayList<>();

        ClassLoader classLoader = SpringLiteApplicationContext.class.getClassLoader();
        packagePath = packagePath.replaceAll("\\.", "/");
        URL resource = classLoader.getResource(packagePath);

        if (null == resource) {
            System.out.println("-------------Path configured is not exist, exit-------------");
            return classList;
        }
        File file;
        try {
            // 路径里面有空格或+等场景，需要toURI().getPath()，直接getFile()可能无法识别
            file = new File(resource.toURI().getPath());
        } catch (URISyntaxException e) {
            System.out.println("-------------Path configured is not exist, exit-------------");
            return classList;
        }
        if (file.isDirectory()) {
            classList = travelFile(classLoader, file);
        }
        return classList;
    }

    /**
     * 循环遍历
     *
     * @param classLoader 类加载器
     * @param file        文件路径
     * @return 所有类
     */
    private List<Class<?>> travelFile(ClassLoader classLoader, File file) {
        List<Class<?>> tmpList = new ArrayList<>();
        File[] files = file.listFiles();
        if (null == files || files.length == 0) {
            return tmpList;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                tmpList.addAll(travelFile(classLoader, f));
            } else {
                String fileName = f.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    Class<?> beanClass = loadClassByName(classLoader, fileName);
                    if (null != beanClass) {
                        tmpList.add(beanClass);
                    }
                }
            }
        }
        return tmpList;
    }

    /**
     * 根据class文件路径加载类
     *
     * @param classLoader 类加载器
     * @param fileName    文件路径
     * @return 类
     */
    private Class<?> loadClassByName(ClassLoader classLoader, String fileName) {
        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class")).replace("\\", ".");
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            System.out.println("-------------Class " + className + " load failed.-------------");
        }
        return null;
    }
}
