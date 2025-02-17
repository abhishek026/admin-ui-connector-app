package admin.ui.connector.ticker;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class AccountIdHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Extract accountId from the URL or query parameters
        String path = request.getURI().getPath();
        String[] parts = path.split("/"); // Assuming path is like /zerodha/ticks/{accountId}
        if (parts.length > 3) {
            String accountId = parts[4]; // Extract the accountId
            attributes.put("accountId", accountId); // Set it as a session attribute
        }
        return true; // Continue the handshake
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No action needed here
    }
}
