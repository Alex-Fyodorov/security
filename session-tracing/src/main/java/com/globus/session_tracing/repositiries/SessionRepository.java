package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer>,
        JpaSpecificationExecutor<Session> {

    @Transactional
    @Modifying
    @Query("update Session s set s.isActive = false, s.logoutTime = current_timestamp where s.id = :sessionId")
    void logout (Integer sessionId);
}
