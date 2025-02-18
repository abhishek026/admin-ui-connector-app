package admin.ui.connector.kiteconnect;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import admin.ui.connector.model.BrokerPositions;
import com.zerodhatech.models.Position;
import com.zerodhatech.ticker.KiteTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.OrderParams;

import admin.ui.connector.dao.BrokerDataDao;
import admin.ui.connector.dao.OrderDataDao;
import admin.ui.connector.model.OrderTemplate;
import admin.ui.connector.model.PositionData;

@Component
public class KiteClientBusiness {
    private static final Logger logger = LogManager.getLogger(KiteClientBusiness.class);

    @Autowired
    private KiteConnectBusiness kiteConnectBusiness;

    @Autowired
    private OrderDataDao orderDataDao;

    @Autowired
    private BrokerDataDao brokerDataDao;

    public void generateTokens(KiteCredentials credentials) {
        try {
            KiteConnect kiteConnect = kiteConnectBusiness.generateTokens(credentials);
            if (kiteConnect.getAccessToken() != null && kiteConnect.getPublicToken() != null) {
                orderDataDao.updateBrokerTokens(kiteConnect, credentials.getBrokerId());
            }
            // kiteConnect.logout();
            //orderDataDao.updateBrokerTokens(kiteConnect, credentials.getBrokerId());
            logger.info("Kite session initialized successfully for user: {}", credentials.getUserId());
        } catch (IOException | KiteException e) {
            logger.error("Failed to initialize Kite session for user: {}", credentials.getUserId(), e);
        }
    }

    public Object getInfo(List<Long> orders) {
        Map<Long, Object> resMap = new HashMap<>();
        for (Long brokerId : orders) {
            KiteConnect kiteConnect;
            try {
                kiteConnect = brokerDataDao.getBrokerTokens(brokerId);
                if (kiteConnect == null) {
                    logger.warn("Skipping broker ID {} due to missing credentials.", brokerId);
                    continue;
                }
                try {
                    logger.info(kiteConnect.getPositions());
                    logger.info(kiteConnect.getProfile().broker);
                    logger.info(kiteConnect.getProfile());
                    logger.info(kiteConnect.getHoldings());
                    logger.info(kiteConnect.getOrders());
                    resMap.put(brokerId, kiteConnect.getProfile());
                } catch (KiteException e) {
                    //  throw new RuntimeException(e);
                }
            } catch (Exception e) {
                logger.error("Failed to retrieve KiteConnect for broker ID {}: {}", brokerId, e.getMessage(), e);
                continue;
            }

        }
        return resMap;
    }


    public void placeOrder(List<OrderTemplate> orders) {
        for (Long brokerId : orders.get(0).getBrokers()) {
            KiteConnect kiteConnect;
            try {
                kiteConnect = brokerDataDao.getBrokerTokens(brokerId);
                if (kiteConnect == null) {
                    logger.warn("Skipping broker ID {} due to missing credentials.", brokerId);
                    continue;
                }
            } catch (Exception e) {
                logger.error("Failed to retrieve KiteConnect for broker ID {}: {}", brokerId, e.getMessage(), e);
                continue;
            }

            for (OrderTemplate order : orders) {
                try {
                    OrderParams orderParams = new OrderParams();
                    orderParams.quantity = order.getQty();
                    orderParams.transactionType = order.getTransactionType();
                    orderParams.tradingsymbol = order.getTradingSymbol();
                    orderParams.exchange = Constants.EXCHANGE_NFO;
                    orderParams.product = Constants.PRODUCT_NRML;
                    orderParams.validity = Constants.VALIDITY_DAY;
                    orderParams.orderType = Constants.ORDER_TYPE_MARKET;

                    kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
                    logger.info("Order placed for {} (Qty: {}) by broker {}",
                            order.getTradingSymbol(), order.getQty(), brokerId);
                } catch (IOException | KiteException e) {
                    logger.error("Order failed for broker {}: {}", brokerId, e.getMessage(), e);
                }
            }
        }
    }

    public List<BrokerPositions> getAllBrokerPositions() {
        List<BrokerPositions> brokerPositionsList = brokerDataDao.getActiveTokenBrokers();

        if (brokerPositionsList == null || brokerPositionsList.isEmpty()) {
            logger.info("No active brokers found.");
            return Collections.emptyList();
        }

        for (BrokerPositions broker : brokerPositionsList) {
            KiteConnect kiteConnect = null;
            try {
                // Initialize KiteConnect for this broker
                kiteConnect = new KiteConnect(broker.getApiKey());
                kiteConnect.setAccessToken(broker.getAccessToken());
                kiteConnect.setPublicToken(broker.getPublicToken());
                kiteConnect.setUserId(broker.getUserId());

                // Fetch positions
                Map<String, List<Position>> positionsMap = kiteConnect.getPositions();
              /*  String accessToken="";String apiKey="";
                KiteTicker kiteTicker=new KiteTicker(accessToken,apiKey);
                kiteTicker.connect()
*/
                // Validate "net" and "day" exist and contain data
                if (positionsMap == null || positionsMap.isEmpty() ||
                        (isListEmpty(positionsMap.get("net")) && isListEmpty(positionsMap.get("day")))) {
                    logger.info("No valid position data available for broker: {}", broker.getBrokerName());
                    broker.setPositions(Collections.emptyList()); // Set empty list instead of null
                    continue;
                }

                broker.setPositions(convertToPositionData(positionsMap));
                orderDataDao.updatePositionDataFromDB(broker.getPositions());
                //kiteConnect.logout();

            } catch (KiteException | IOException e) {
                logger.error("Error fetching positions for broker {}: {}", broker.getBrokerName(), e.getMessage());
                broker.setPositions(Collections.emptyList()); // Set empty list to avoid null issues
            }
        }
        return brokerPositionsList;
    }


    private List<PositionData> convertToPositionData(Map<String, List<Position>> positionsMap) {
        if (positionsMap == null || positionsMap.isEmpty()) {
            return Collections.emptyList();
        }

        return positionsMap.entrySet().stream()
                .filter(entry -> "net".equals(entry.getKey()) || "day".equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(pos -> {
                    PositionData positionData = new PositionData();
                    positionData.setTradingSymbol(pos.tradingSymbol);
                    positionData.setInstrumentToken(pos.instrumentToken);
                    return positionData;
                })
                .collect(Collectors.toList());
    }

    private boolean isListEmpty(List<Position> list) {
        return list == null || list.isEmpty();
    }




}
