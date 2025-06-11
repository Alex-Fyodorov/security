package com.globus.session_tracing.repositiries.specifications;

import com.globus.session_tracing.entities.Session;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class SessionSpecification {

    public static Specification<Session> userIdEqualTo(Integer userId) {
        return (((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), userId)));
    }

    public static Specification<Session> loginTimeGreaterOrEqualThan(LocalDateTime loginTime) {
        return (((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("loginTime"), loginTime)));
    }

    public static Specification<Session> methodEqualTo(String method) {
        return (((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("method"), method)));
    }

    public static Specification<Session> activityEqualTo(Boolean isActive) {
        return (((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isActive"), isActive)));
    }
}
