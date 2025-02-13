package admin.ui.connector.kiteconnect;

import admin.ui.connector.dao.BrokerDataDao;
import admin.ui.connector.dao.OrderDataDao;
import admin.ui.connector.model.OrderTemplate;
import admin.ui.connector.utills.SSLUtil;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.OrderParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    public void getInfo(List<OrderTemplate> orders) {
        for (Long brokerId : Arrays.asList(1L)) {
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
                    logger.info(kiteConnect.getHoldings());
                    logger.info(kiteConnect.getOrders());
                } catch (KiteException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                logger.error("Failed to retrieve KiteConnect for broker ID {}: {}", brokerId, e.getMessage(), e);
                continue;
            }

        }
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
}
