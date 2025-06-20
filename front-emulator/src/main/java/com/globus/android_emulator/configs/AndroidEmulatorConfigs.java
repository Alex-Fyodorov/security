package com.globus.android_emulator.configs;

import com.globus.android_emulator.integrations.BiometricServiceIntegrationProperties;
import com.globus.android_emulator.integrations.SessionServiceIntegrationProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({BiometricServiceIntegrationProperties.class,
        SessionServiceIntegrationProperties.class})
public class AndroidEmulatorConfigs {
    private final BiometricServiceIntegrationProperties biometricServiceIntegrationProperties;
    private final SessionServiceIntegrationProperties sessionServiceIntegrationProperties;

    @Bean
    public WebClient biometricServiceWebClient() {
        TcpClient tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        biometricServiceIntegrationProperties.getConnectTimeout())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(
                            biometricServiceIntegrationProperties.getReadTimeout(), TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(
                            biometricServiceIntegrationProperties.getWriteTimeout(), TimeUnit.MILLISECONDS));
                });
        return WebClient
                .builder()
                .baseUrl(biometricServiceIntegrationProperties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .build();
    }

    @Bean
    public WebClient sessionTracingServiceWebClient() {
        TcpClient tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        sessionServiceIntegrationProperties.getConnectTimeout())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(
                            sessionServiceIntegrationProperties.getReadTimeout(), TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(
                            sessionServiceIntegrationProperties.getWriteTimeout(), TimeUnit.MILLISECONDS));
                });
        return WebClient
                .builder()
                .baseUrl(sessionServiceIntegrationProperties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .build();
    }
}
