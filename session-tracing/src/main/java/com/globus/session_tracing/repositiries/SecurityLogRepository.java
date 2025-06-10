package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.SecurityLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityLogRepository extends CrudRepository<SecurityLog, Integer> {
}
