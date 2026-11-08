package cz.home.task_manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthenticationManager authenticationManager;

    public WebSocketConfig(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // Konfigurace zpráv a jejich směrování
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // Broker pro zprávy odesílané klientům
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix pro zprávy přicházející od klientů na server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix pro zprávy adresované konkrétnímu přihlášenému uživateli
        config.setUserDestinationPrefix("/user");
    }

    // Ověření uživatele při navázání STOMP spojení
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor
                        = MessageHeaderAccessor.getAccessor(
                                message,
                                StompHeaderAccessor.class
                        );
                // Ověření provádíme pouze při navázání spojení
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    String username
                            = accessor.getFirstNativeHeader("username");

                    String password
                            = accessor.getFirstNativeHeader("password");

                    if (username == null || password == null) {
                        throw new BadCredentialsException(
                                "Chybí přihlašovací údaje.");
                    }

                    // Ověření přihlašovacích údajů pomocí Spring Security
                    Authentication authentication
                            = authenticationManager.authenticate(
                                    new UsernamePasswordAuthenticationToken(
                                            username,
                                            password
                                    )
                            );

                    // Přiřazení ověřeného uživatele k WebSocket spojení
                    accessor.setUser(authentication);
                }

                return message;
            }
        });
    }

    // Registrace WebSocket endpointu
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

}
