package admin.ui.connector.dao;

import admin.ui.connector.model.Broker;
import admin.ui.connector.model.OrderData;
import admin.ui.connector.model.OrderTemplate;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDataDao {

    private final Logger logger = LogManager.getLogger(OrderDataDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String GET_EXPIRY_DATES_QUERY = new StringBuilder()
            .append("SELECT DISTINCT expiry AS expiry_key, CONCAT(TO_CHAR(expiry, 'DD Mon'), ")
            .append("CASE WHEN EXTRACT(YEAR FROM expiry) > EXTRACT(YEAR FROM CURRENT_DATE) ")
            .append("THEN ' ' || EXTRACT(YEAR FROM expiry) ELSE '' END, ")
            .append("'(', expiry - CURRENT_DATE, ' days)') AS expiry_value FROM order_data ")
            .append("WHERE expiry IS NOT NULL AND exchange = 'NFO' AND name = 'NIFTY' ")
            .append("AND instrument_type IN ('CE', 'PE') AND expiry >= CURRENT_DATE ")
            .append("ORDER BY expiry ASC;").toString();


    private static final String GET_ORDER_DATA_BY_EXPIRY_QUERY = new StringBuilder()
            .append("SELECT DISTINCT expiry AS expiry_key, ")
            .append("CONCAT(TO_CHAR(expiry, 'DD Mon'), '(', expiry - CURRENT_DATE, ' days)') AS expiry_value, ")
            .append("strike, instrument_type AS inst_type, lot_size AS lot, ")
            .append("exchange_token, tradingsymbol, instrument_token, exchange ")
            .append("FROM order_data WHERE expiry = ? AND exchange = 'NFO' ")
            .append("AND name = 'NIFTY' AND instrument_type IN ('CE', 'PE');")
            .toString();


    public Map<String, String> getExpiryDates() {
        Map<String, String> expiryDateMap = new LinkedHashMap<>();

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(GET_EXPIRY_DATES_QUERY);
        for (Map<String, Object> row : resultList) {
            Date expiryDate = (Date) row.get("expiry_key");
            String expiryKey = expiryDate.toString();
            String expiryValue = (String) row.get("expiry_value");
            expiryDateMap.put(expiryKey, expiryValue);
        }

        return expiryDateMap;
    }

    public List<OrderData> getOrderDataByExpiry(String expiry) {
        Date expiryDate = Date.valueOf(expiry);

        return jdbcTemplate.query(GET_ORDER_DATA_BY_EXPIRY_QUERY, new Object[]{expiryDate}, new OrderDataRowMapper());
    }

    private static class OrderDataRowMapper implements RowMapper<OrderData> {
        @Override
        public OrderData mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderData orderData = new OrderData();
            orderData.setExchangeToken(rs.getLong("exchange_token"));
            orderData.setInstrumentToken(rs.getLong("instrument_token"));
            orderData.setTradingSymbol(rs.getString("tradingsymbol"));
            orderData.setExpiry(rs.getString("expiry_key"));
            orderData.setExpiryValue(rs.getString("expiry_value"));
            orderData.setStrike(Double.valueOf(rs.getDouble("strike")).intValue());
            orderData.setInstType(rs.getString("inst_type"));
            orderData.setLotSize(rs.getInt("lot"));
            orderData.setExchange(rs.getString("exchange"));
            return orderData;
        }
    }


    public List<Broker> getActiveBrokers() {
        String query = "SELECT * FROM brokers WHERE is_active = TRUE order by account";

        return jdbcTemplate.query(query, new RowMapper<Broker>() {
            @Override
            public Broker mapRow(ResultSet rs, int rowNum) throws SQLException {
                Broker broker = new Broker();
                broker.setBrokerId(rs.getInt("broker_id"));
                broker.setBrokerName(rs.getString("broker_name"));
                broker.setDisplayName(rs.getString("display_name"));
                broker.setAccount(rs.getString("account"));
                broker.setBrokerUrl(rs.getString("broker_url"));
                broker.setPublicToken(rs.getString("public_token"));
                broker.setAccessToken(rs.getString("access_token"));
                broker.setActive(rs.getBoolean("is_active"));
                return broker;
            }
        });
    }

    public void saveOrderTemplates(List<OrderTemplate> orderTemplates) {
        String sql = "INSERT INTO order_template (exchange_token, instrument_token, trading_symbol, expiry, expiry_value, " +
                "strike, inst_type, lot_size, stop_loss, broker, lots, qty, exchange, order_type, transaction_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrderTemplate order = orderTemplates.get(i);
                ps.setLong(1, order.getExchangeToken());
                ps.setLong(2, order.getInstrumentToken());
                ps.setString(3, order.getTradingSymbol());
                ps.setDate(4, java.sql.Date.valueOf(order.getExpiry()));
                ps.setString(5, order.getExpiryValue());
                ps.setBigDecimal(6, order.getStrike());
                ps.setString(7, order.getInstType());
                ps.setInt(8, order.getLotSize());
                ps.setBigDecimal(9, order.getStopLoss());
                ps.setInt(10, order.getBroker());
                ps.setInt(11, order.getLots());
                ps.setInt(12, order.getQty());
                ps.setString(13, order.getExchange());
                ps.setString(14, order.getOrderType());
                ps.setString(15, order.getTransactionType());
            }

            @Override
            public int getBatchSize() {
                return orderTemplates.size();
            }
        });
    }

    public void updateBrokerTokens(KiteConnect kiteConnect, Long brokerId) {
        String query = "UPDATE brokers " +
                "SET api_key=? , user_id=? , public_token = ?, access_token = ?, updated_date = NOW() " +
                "WHERE broker_id = ?";

        int rowsAffected = jdbcTemplate.update(query, kiteConnect.getApiKey(), kiteConnect.getUserId(), kiteConnect.getPublicToken(), kiteConnect.getAccessToken(), brokerId);

        if (rowsAffected > 0) {
            logger.info("Successfully updated tokens for broker ID: {}", brokerId);
        } else {
            logger.warn("No broker found with ID: {}", brokerId);
        }
    }

}


