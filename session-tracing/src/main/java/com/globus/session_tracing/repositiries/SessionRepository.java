package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>,
        JpaSpecificationExecutor<Session> {

    @Transactional
    @Modifying
    @Query("update Session s set s.isActive = false, s.logoutTime = current_timestamp where s.id = :sessionId and s.isActive = true")
    void logout (Long sessionId);

    @Query("select count(s.id) from Session s where s.userId = :userId and isActive = true")
    int sessionCount(int userId);


}
