package com.globus.modul26.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class JwtAuthTokenFilterTest {

    private JwtAuthTokenFilter filter;
    private JwtService jwtService;
    private JwtBlacklistService blacklistService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthTokenFilter();

        jwtService = mock(JwtService.class);
        blacklistService = mock(JwtBlacklistService.class);

        // Подключаем моки через reflection, так как @Autowired поля приватные
        try {
            var jwtField = JwtAuthTokenFilter.class.getDeclaredField("jwtService");
            jwtField.setAccessible(true);
            jwtField.set(filter, jwtService);

            var blField = JwtAuthTokenFilter.class.getDeclaredField("blacklistService");
            blField.setAccessible(true);
            blField.set(filter, blacklistService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void doFilterInternal_blacklistedToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer SOME_TOKEN");
        when(jwtService.extractJti("SOME_TOKEN")).thenReturn("JTI123");
        when(blacklistService.isBlacklisted("JTI123")).thenReturn(true);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/json");
        pw.flush();
        assertTrue(sw.toString().contains("Вы не авторизированы."));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_notBlacklisted_callsChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer VALID_TOKEN");
        when(jwtService.extractJti("VALID_TOKEN")).thenReturn("JTI456");
        when(blacklistService.isBlacklisted("JTI456")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_callsChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doFilterInternal_headerNotStartsWithBearer_callsChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic WHATEVER");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}