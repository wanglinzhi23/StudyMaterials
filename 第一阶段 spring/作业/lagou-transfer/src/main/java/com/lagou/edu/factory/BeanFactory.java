package com.lagou.edu.factory;

import com.lagou.edu.annotation.Autowired;
import com.lagou.edu.annotation.Component;
import com.lagou.edu.annotation.Repository;
import com.lagou.edu.annotation.Service;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.reflections.Reflections;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 应癫
 *
 * 工厂类，生产对象（使用反射技术）
 */
public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static final Map<String,Object> beanContainer = new HashMap<>();  // 存储对象

	public static void init(String packageName) throws InstantiationException, IllegalAccessException {
		Reflections f = new Reflections(packageName);
		Set<Class<?>> comp = f.getTypesAnnotatedWith(Component.class);
		Set<Class<?>> service = f.getTypesAnnotatedWith(Service.class);
		Set<Class<?>> repository = f.getTypesAnnotatedWith(Repository.class);
		for (Class<?> c : service) {
			Object bean = c.newInstance();
			Service annotation = c.getAnnotation(Service.class);
			String beanId=annotation.value()==null? c.getName() : annotation.value();
			if(beanContainer.get(beanId)==null){
				beanContainer.put(beanId, bean);
			}

		}
		for(Class<?> c : repository){
			Object bean = c.newInstance();
			Repository annotation= c.getAnnotation(Repository.class);
			String beanId=annotation.value()==null? c.getName() : annotation.value();
			if(beanContainer.get(beanId)==null){
				beanContainer.put(beanId, bean);
			}
		}
		for(Class<?> c : comp){
			Object bean = c.newInstance();
			Component annotation= c.getAnnotation(Component.class);
			String beanId=annotation.value()==null? c.getName() : annotation.value();
			if(beanContainer.get(beanId)==null){
				beanContainer.put(beanId, bean);
			}
		}
		beanContainer.forEach((key,value)->{
			Class c=value.getClass();
			Field[]fields=c.getDeclaredFields();
			//返回值用于判断是否完成这个方法
			boolean b=false;
			//遍历域
			for (Field field:fields) {
				//获取域中的注解，遍历注解
				Annotation []anns=field.getAnnotations();
				for (Annotation ann:anns) {
					//这里使用instanceof关键字，判断注解中是否包含MyAutowired
					if (ann instanceof Autowired){
						System.out.println(field.getName()+"--这个域使用了我的注解");
						//f.getGenericType()：获取该域的类型
						System.out.println(field.getGenericType().toString()+"--这个域的类型");
						//转成Class
						Class c2= (Class) field.getGenericType();
						try {//创建实例
							Object o2= beanContainer.get(c2.getName());
							field.set(value,o2);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}

				}
			}
		});
	}

    static {
        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
        // 加载xml
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        // 解析xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> beanList = rootElement.selectNodes("//bean");
            for (int i = 0; i < beanList.size(); i++) {
                Element element =  beanList.get(i);
                // 处理每个bean元素，获取到该元素的id 和 class 属性
                String id = element.attributeValue("id");        // accountDao
                String clazz = element.attributeValue("class");  // com.lagou.edu.dao.impl.JdbcAccountDaoImpl
                // 通过反射技术实例化对象
                Class<?> aClass = Class.forName(clazz);
                Object o = aClass.newInstance();  // 实例化之后的对象

                // 存储到map中待用
	            beanContainer.put(id,o);

            }

            // 实例化完成之后维护对象的依赖关系，检查哪些对象需要传值进入，根据它的配置，我们传入相应的值
            // 有property子元素的bean就有传值需求
            List<Element> propertyList = rootElement.selectNodes("//property");
            // 解析property，获取父元素
            for (int i = 0; i < propertyList.size(); i++) {
                Element element =  propertyList.get(i);   //<property name="AccountDao" ref="accountDao"></property>
                String name = element.attributeValue("name");
                String ref = element.attributeValue("ref");

                // 找到当前需要被处理依赖关系的bean
                Element parent = element.getParent();

                // 调用父元素对象的反射功能
                String parentId = parent.attributeValue("id");
                Object parentObject = beanContainer.get(parentId);
                // 遍历父对象中的所有方法，找到"set" + name
                Method[] methods = parentObject.getClass().getMethods();
                for (int j = 0; j < methods.length; j++) {
                    Method method = methods[j];
                    if(method.getName().equalsIgnoreCase("set" + name)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
                        method.invoke(parentObject,beanContainer.get(ref));
                    }
                }

                // 把处理之后的parentObject重新放到map中
	            beanContainer.put(parentId,parentObject);

            }


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static  Object getBean(String id) {
        return beanContainer.get(id);
    }

}
