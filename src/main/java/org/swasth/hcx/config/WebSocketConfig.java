package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${hcx_application.url}")
    private String urlPattern;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("Allowed Origins for WebSocket " + getAllowedOrigins().toString());
        registry.addEndpoint("/ws").setAllowedOriginPatterns(getAllowedOrigins()).withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

    private String[] getAllowedOrigins() {
        // Define the base URL and add a wildcard for any subpath
        return new String[] {urlPattern + "*"};
    }
}

