package com.lagou.edu.annotation;

import com.lagou.edu.utils.TransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author Linzhi.Wang
 * @version V1.0
 * @Title: transactionalAspect.java
 * @Package com.lagou.edu.annotation
 * @Description
 * @date 2020 04-13 15:55.
 */
@Aspect
@Component
public class transactionalAspect {
	@Autowired
	private TransactionManager transactionManager;
	@Around(value = "@annotation(com.lagou.edu.annotation.Transactional)")
	void execute(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		transactionManager.beginTransaction();
		try {
			proceedingJoinPoint.proceed();
			transactionManager.commit();
		}catch (Exception e){
			transactionManager.rollback();
		}

	}
}
