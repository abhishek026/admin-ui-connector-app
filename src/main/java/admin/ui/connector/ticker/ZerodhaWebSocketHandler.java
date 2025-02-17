package admin.ui.connector.ticker;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ZerodhaWebSocketHandler extends TextWebSocketHandler {

    private final ZerodhaTickerService zerodhaTickerService;

    public ZerodhaWebSocketHandler(ZerodhaTickerService zerodhaTickerService) {
        this.zerodhaTickerService = zerodhaTickerService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String accountId = (String) session.getAttributes().get("accountId");
        zerodhaTickerService.addWebSocketSession(accountId,session); // Add session to the service
        System.out.println("WebSocket connected for account: " + accountId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String accountId = (String) session.getAttributes().get("accountId");
        System.out.println("Received message for account " + accountId + ": " + message.getPayload());
        session.sendMessage(new TextMessage("Live Data for account " + accountId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String accountId = (String) session.getAttributes().get("accountId");
        zerodhaTickerService.removeWebSocketSession(accountId,session); // Remove session after connection closed
        System.out.println("WebSocket closed for account.");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Error on WebSocket for account: " + exception.getMessage());
    }
}
