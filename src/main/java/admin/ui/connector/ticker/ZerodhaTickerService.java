package admin.ui.connector.ticker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Position;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;

import admin.ui.connector.dao.OrderDataDao;
import admin.ui.connector.model.Broker;
import admin.ui.connector.model.PositionData;

@Service
public class ZerodhaTickerService {

	private static final Object NET = "net";
	private static final Object DAY = "day";

	private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
	private final Map<String, KiteTicker> tickerConnections = new ConcurrentHashMap<>();
	private final Map<String, Map<String, PositionData>> accountPositions = new ConcurrentHashMap<>();
	private final Map<String, AccountCredentials> accountCredentialsMap = new ConcurrentHashMap<>();
	@Autowired
	private OrderDataDao orderDataDao;

	static class AccountCredentials {
		String apiKey;
		String accessToken;

		AccountCredentials(String apiKey, String accessToken) {
			this.apiKey = apiKey;
			this.accessToken = accessToken;
		}
	}

	public void startTickerForAccountV2(String brokerId, ArrayList<Long> tokens) {
		if (tickerConnections.containsKey(brokerId)) {
			return; // Ticker already running for this account
		}
		Broker broker = orderDataDao.FindByBrokerId(Integer.parseInt(brokerId)).get(0);
		KiteTicker tickerProvider = new KiteTicker(broker.getAccessToken(), broker.getApiKey());
		tickerConnections.put(brokerId, tickerProvider);
		accountCredentialsMap.put(brokerId, new AccountCredentials(broker.getApiKey(), broker.getAccessToken()));

		tickerProvider.setOnConnectedListener(() -> {
			System.out.println("Connected to Zerodha for account: " + brokerId);
			tickerProvider.subscribe(tokens);
			tickerProvider.setMode(tokens, KiteTicker.modeFull);
		});

		tickerProvider.setOnDisconnectedListener(() -> {
			System.out.println("Disconnected from Zerodha for account: " + brokerId);
		});

		tickerProvider.setOnTickerArrivalListener(ticks -> processTicks(brokerId, ticks));

		tickerProvider.setTryReconnection(true);
		try {
			tickerProvider.setMaximumRetries(10);
			tickerProvider.setMaximumRetryInterval(30);
		} catch (KiteException e) {
			e.printStackTrace();
		}
		tickerProvider.connect();
		fetchAndStorePositions(brokerId, broker.getAccessToken(), broker.getApiKey());
	}

	public void fetchAndStorePositions(String accountId, String accessToken, String apiKey) {
		KiteConnect kiteConnect = new KiteConnect(apiKey);
		kiteConnect.setAccessToken(accessToken);
		try {
			Map<String, List<Position>> positions = kiteConnect.getPositions();
			if (!positions.isEmpty()) {
				Map<String, PositionData> positionMap = positions.entrySet().stream()
						.filter(entry -> NET.equals(entry.getKey()) || DAY.equals(entry.getKey()))
						.map(Map.Entry::getValue).filter(Objects::nonNull).flatMap(List::stream)
						.filter(Objects::nonNull).map(pos -> {
							return convertToPositionDto(pos);
						}).collect(Collectors.toMap(PositionData::getInstrumentToken, Function.identity()));
				accountPositions.put(accountId, positionMap);

			}
		} catch (KiteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PositionData convertToPositionDto(Position pos) {
		PositionData positionData = new PositionData();
		positionData.setTradingSymbol(pos.tradingSymbol);
		positionData.setInstrumentToken(pos.instrumentToken);
		positionData.setBuyPrice(pos.buyPrice);
		positionData.setSellPrice(pos.sellPrice);
		positionData.setBQty(pos.buyQuantity);
		positionData.setSQty(pos.sellQuantity);
		positionData.setNQty(pos.netQuantity);
		return positionData;
	}

	private void processTicks(String accountId, ArrayList<Tick> ticks) {
		if (ticks.isEmpty()) {
			return;
		}
		Tick tick = ticks.get(0);
		System.out.println("ticker  for "+accountId+" "+tick.getLastTradedPrice());
		broadcastToWebSocketClients(accountId, preparedResDataSet(tick, accountId));
	}

	public void startTickerForAccount(String accountId, String apiKey, String accessToken, ArrayList<Long> tokens) {
		if (tickerConnections.containsKey(accountId)) {
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
				broadcastToWebSocketClients(accountId, preparedResDataSet(tick, accountId));
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

	private Map<String, Object> preparedResDataSet(Tick tick, String accountId) {
		Map<String, PositionData> positionMap = accountPositions.get(accountId);
		Map<String, Object> resMap = new ConcurrentHashMap<String, Object>();
		int quantity=0;
		double pnl=0;
		if (positionMap != null) {
			PositionData position = positionMap.get(tick.getInstrumentToken() + "");
			if (position != null) {
				double buyPrice = position.getBuyPrice();
				quantity = position.getNQty();
				double currentPrice = tick.getLastTradedPrice();
				pnl = (currentPrice - buyPrice) * quantity;
				resMap.put("bPrice", position.getBuyPrice() > 0 ? position.getBuyPrice() : position.getSellPrice());
				resMap.put("OrderType", position.getBuyPrice() > 0 ? 'B' : 'S');
			}
		}
		resMap.put("accountId", accountId);
		resMap.put("qty", quantity);
		resMap.put("ltp", tick.getLastTradedPrice());
		resMap.put("avg", tick.getAverageTradePrice());
		resMap.put("pnl", pnl);
		resMap.put("instToken", tick.getInstrumentToken());
		return resMap;

	}

	public void stopTickerForAccount(String accountId) {
		KiteTicker tickerProvider = tickerConnections.get(accountId);
		if (tickerProvider != null) {
			tickerProvider.disconnect();
			tickerConnections.remove(accountId);
			sessions.remove(accountId);
			accountCredentialsMap.remove(accountId);
			System.out.println("Ticker disconnected for account: " + accountId);
		}
	}

	public void addWebSocketSession(String accountId, WebSocketSession session) {
		sessions.computeIfAbsent(accountId, k -> ConcurrentHashMap.newKeySet()).add(session);
	}

	public void removeWebSocketSession(String accountId, WebSocketSession session) {
		Set<WebSocketSession> wsession = sessions.get(accountId);
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

	@Scheduled(fixedRate = 600000)
	public void refreshAllPositions() {
		System.out.println("Update Positions in avery 30 seconds");
		accountCredentialsMap.forEach((accountId, credentials) -> fetchAndStorePositions(accountId,
				credentials.accessToken, credentials.apiKey));
	}

}
