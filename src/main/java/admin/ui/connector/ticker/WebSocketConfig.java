package admin.ui.connector.ticker;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ZerodhaWebSocketHandler zerodhaWebSocketHandler;

    public WebSocketConfig(ZerodhaWebSocketHandler zerodhaWebSocketHandler) {
        this.zerodhaWebSocketHandler = zerodhaWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(zerodhaWebSocketHandler, "/zerodha/ticks/{accountId}")
                .setAllowedOrigins("*")
                .addInterceptors(new AccountIdHandshakeInterceptor());
    }
}

