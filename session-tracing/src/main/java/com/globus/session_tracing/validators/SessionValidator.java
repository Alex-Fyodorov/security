package com.globus.session_tracing.validators;

import com.globus.session_tracing.dtos.SessionDto;
import com.globus.session_tracing.exceptions.SessionsOperationsException;
import org.springframework.stereotype.Component;

@Component
public class SessionValidator {

    public void validate(SessionDto sessionDto) {
        if (sessionDto.getUserId() == null) {
            throw new SessionsOperationsException(
                    "В запросе на создание сессии не заполнено поле userId");
        }
    }
}
