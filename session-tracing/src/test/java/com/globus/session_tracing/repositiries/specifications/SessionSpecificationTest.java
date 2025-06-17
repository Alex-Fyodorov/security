package com.globus.session_tracing.repositiries.specifications;

import com.globus.session_tracing.entities.Session;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionSpecificationTest {

    private Root<Session> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void userIdEqualTo() {
        Integer userId = 42;
        Path<Integer> path = mock(Path.class); // RAW mock!
        Predicate predicate = mock(Predicate.class);

        when(root.get("userId")).thenReturn((Path) path); // ВАЖНО: raw cast!
        when(cb.equal(path, userId)).thenReturn(predicate);

        Predicate result = SessionSpecification.userIdEqualTo(userId)
                .toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(predicate, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void loginTimeGreaterOrEqualThan() {
        LocalDateTime loginTime = LocalDateTime.now();
        Path<LocalDateTime> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("loginTime")).thenReturn((Path) path);
        when(cb.greaterThanOrEqualTo(path, loginTime)).thenReturn(predicate);

        Predicate result = SessionSpecification.loginTimeGreaterOrEqualThan(loginTime)
                .toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(predicate, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void methodEqualTo() {
        String method = "test-method";
        Path<String> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("method")).thenReturn((Path) path);
        when(cb.equal(path, method)).thenReturn(predicate);

        Predicate result = SessionSpecification.methodEqualTo(method)
                .toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(predicate, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void activityEqualTo() {
        Boolean isActive = true;
        Path<Boolean> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("isActive")).thenReturn((Path) path);
        when(cb.equal(path, isActive)).thenReturn(predicate);

        Predicate result = SessionSpecification.activityEqualTo(isActive)
                .toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(predicate, result);
    }
}