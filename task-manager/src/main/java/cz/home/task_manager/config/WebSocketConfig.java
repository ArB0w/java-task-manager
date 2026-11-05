package cz.home.task_manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Konfigurace zpráv a jejich směrování
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // Prefix pro zprávy odesílané klientům
        config.enableSimpleBroker("/topic");

        // Prefix pro zprávy přicházející od klientů
        config.setApplicationDestinationPrefixes("/app");
    }

    // Registrace WebSocket endpointu
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}