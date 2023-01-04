package org.example.quoters;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProfilingHandlerBeanPostProcessor implements BeanPostProcessor {
    
    
    private Map<String, Class>  map = new HashMap<>();

    private ProfilingController controller = new ProfilingController();

    public ProfilingHandlerBeanPostProcessor() throws Exception {

        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        platformMBeanServer.registerMBean(controller, new ObjectName("profiling", "name", "controller"));
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Class<?> beanClass = bean.getClass();
        beanClass.getAnnotations();


        if(beanClass.isAnnotationPresent(Profiling.class)){
           map.put(beanName, beanClass);
            System.out.println("ok");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        Class beanClass = map.get(beanName);
        if (beanClass != null){
            return Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                   if(controller.isEnabled()){
                       System.out.println("PROFILIRUIU ...");

                       long before = System.nanoTime();
                       Object retVal = method.invoke(bean,objects);

                       long after = System.nanoTime();
                       System.out.println(after-before);
                       System.out.println("VSE");

                       return retVal;
                   }else{

                      return method.invoke(bean, objects);
                   }
                }
            });
        }
        return bean;
    }
}
