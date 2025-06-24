package com.globus.biometric_auth_service.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {
    private final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut(value = "execution(public * com.pm.biometric_auth_service.controller.*Controller.*(..))")
    public void controllerPointcut() {
    }

    @Pointcut(value = "execution(public * com.pm.biometric_auth_service.service..*.*(..))")
    public void servicePointcut() {
    }

    @Before("controllerPointcut()")
    public void loggingControllers(JoinPoint joinPoint) {
        String controllerName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String path = request.getRequestURI();
        log.info("Controller: %s Called method %s with a path %s".formatted(controllerName, methodName, path));
    }

    @Before("servicePointcut()")
    public void loggingServices(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String params = Arrays.stream(joinPoint.getArgs()).map(Object::toString).collect(Collectors.joining(","));

        if (params.isEmpty()) {
            params = "without params";
        }
        log.info("Service: %s Called method %s with params - %s".formatted(className, methodName, params));
    }

    @AfterReturning(value = "servicePointcut()", returning = "response")
    public void loggingServicesReturn(JoinPoint joinPoint, Object response) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        if (response == null) {
            log.info("%s from %s returns nothing through controller".formatted(methodName, className));
        } else if (response instanceof List<?> responseList) {
            log.info("%s from %s returns list of objects through controller. Examples:".formatted(methodName, className));
            responseList.stream()
                    .limit(10)
                    .map(Object::toString)
                    .forEach(log::info);
        } else {
            log.info("%s from %s returns through controller: %s".formatted(methodName, className, response));
        }
    }

    @After(value = "@annotation(exceptionHandler) && execution(public * com.pm.biometric_auth_service.controller.*Advice.*(..))")
    public void loggingControllerAdvices(JoinPoint joinPoint, ExceptionHandler exceptionHandler) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        StringBuilder exception = new StringBuilder();
        StringBuilder message = new StringBuilder();
        Optional<Throwable> exceptionsOp = Arrays.stream(joinPoint.getArgs()).filter(arg -> arg instanceof Throwable).map(arg -> (Throwable) arg).findFirst();
        if (exceptionsOp.isPresent()) {
            exception.append(exceptionsOp.get().getClass().getName());
            message.append(exceptionsOp.get().getMessage());
        }
        log.info("Exception intercepted {}, message: {}, controller: {}, method: {}", exception, message, className, methodName);
    }

    @AfterThrowing(value = "execution(* com.pm.biometric_auth_service.service.*.*.*(..))", throwing = "exception")
    public void afterThrowingAdvice(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.warn("Exception {} in method {}, class: {}. Message: {}", exception.getClass().getName(), methodName, className, exception.getMessage());
    }
}
