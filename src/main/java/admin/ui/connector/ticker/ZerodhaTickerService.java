package admin.ui.connector.ticker;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;

@Service
public class ZerodhaTickerService {

	private final Map<String, KiteTicker> tickerConnections = new ConcurrentHashMap<>();
	private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

//	public ZerodhaTickerService(Set<WebSocketSession> sessions) {
//		this.sessions = sessions;
//	}

	public void startTickerForAccount(String accountId, String apiKey, String accessToken, ArrayList<Long> tokens) {
		if(tickerConnections.containsKey(accountId)) {
			return;
		}
		KiteTicker tickerProvider = new KiteTicker(accessToken, apiKey);

		// Store the KiteTicker instance per account
		tickerConnections.put(accountId, tickerProvider);

		tickerProvider.setOnConnectedListener(() -> {
			System.out.println("Connected to Zerodha for account: " + accountId);
			tickerProvider.subscribe(tokens);
			tickerProvider.setMode(tokens, KiteTicker.modeFull);
		});

		tickerProvider.setOnDisconnectedListener(() -> {
			System.out.println("Disconnected from Zerodha for account: " + accountId);
		});

		tickerProvider.setOnTickerArrivalListener(ticks -> {
			if (!ticks.isEmpty()) {
				Tick tick = ticks.get(0);
				System.out.println("broadcastToWebSocketClients ::" + tick.getAverageTradePrice());
				broadcastToWebSocketClients(accountId, preparedResDataSet(tick,accountId));
			}
		});

		tickerProvider.setTryReconnection(true);
		try {
			tickerProvider.setMaximumRetries(10);
			tickerProvider.setMaximumRetryInterval(30);
		} catch (KiteException e) {
			e.printStackTrace();
		}

		tickerProvider.connect();
	}

	private Map<String,Object> preparedResDataSet(Tick tick, String accountId) {
		Map<String,Object> resMap=new ConcurrentHashMap<String, Object>();
		resMap.put("accountId", accountId);
		resMap.put("qty", 10);
		resMap.put("ltp", tick.getLastTradedPrice());
		resMap.put("avg", tick.getAverageTradePrice());
		resMap.put("pnl", tick.getOpenPrice());
		resMap.put("instToken", tick.getInstrumentToken());
		return resMap;
	}

	public void stopTickerForAccount(String accountId) {
		KiteTicker tickerProvider = tickerConnections.get(accountId);
		if (tickerProvider != null) {
			tickerProvider.disconnect();
			tickerConnections.remove(accountId);
			System.out.println("Ticker disconnected for account: " + accountId);
		}
	}

	public void addWebSocketSession(String accountId, WebSocketSession session) {
		sessions.computeIfAbsent(accountId, k -> ConcurrentHashMap.newKeySet()).add(session);
	}

	public void removeWebSocketSession(String accountId, WebSocketSession session) {
		Set<WebSocketSession>  wsession=sessions.get(accountId);
		if (wsession != null) {
			wsession.remove(session);
			if (wsession.isEmpty()) {
				sessions.remove(accountId); // Clean up empty account entries
			}
		}
	}
	private void broadcastToWebSocketClients(String accountId, Map<String, Object> response) {
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        String jsonResponse = objectMapper.writeValueAsString(response);
	        Set<WebSocketSession> wsession = sessions.get(accountId);
	        if (wsession != null) {
	            for (WebSocketSession session : wsession) {
	                try {
	                    session.sendMessage(new TextMessage(jsonResponse));
	                    System.out.println("broadcastToWebSocketClients ::" + jsonResponse);
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
