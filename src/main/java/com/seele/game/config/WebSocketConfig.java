package com.seele.game.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置
 * 用于PVP实时对战
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于向客户端广播消息
        // /topic - 广播消息（一对多）
        // /queue - 点对点消息（一对一）
        config.enableSimpleBroker("/topic", "/queue");

        // 客户端发送消息的目的地前缀
        config.setApplicationDestinationPrefixes("/app");

        // 点对点消息的用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点
        registry.addEndpoint("/ws/pvp")
                .setAllowedOriginPatterns("*") // 允许跨域（生产环境需要配置具体域名）
                .withSockJS(); // 支持SockJS fallback
    }
}
