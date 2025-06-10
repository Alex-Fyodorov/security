package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<Session, Integer> {

    @Transactional
    @Modifying
    @Query("update Session s set s.isActive = false, s.logoutTime = current_timestamp where s.id = :sessionId")
    void logout (Integer sessionId);
}
