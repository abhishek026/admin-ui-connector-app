package admin.ui.connector.dao;

import admin.ui.connector.model.*;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final String GET_ORDER_DATA_BY_TRADING_SYMBOL = new StringBuilder()
            .append("SELECT DISTINCT expiry AS expiry_key, ")
            .append("CONCAT(TO_CHAR(expiry, 'DD Mon'), '(', expiry - CURRENT_DATE, ' days)') AS expiry_value, ")
            .append("strike, instrument_type AS inst_type, lot_size AS lot, ")
            .append("exchange_token, tradingsymbol, instrument_token, exchange ")
            .append("FROM order_data WHERE tradingsymbol = ? ")
            .append(" Limit 1;")
            .toString();

    private static final String GET_ORDER_DATA_BY_TRADING_SYMBOL_ALL = new StringBuilder()
            .append("SELECT DISTINCT expiry AS expiry_key, ")
            .append("CONCAT(TO_CHAR(expiry, 'DD Mon'), '(', expiry - CURRENT_DATE, ' days)') AS expiry_value, ")
            .append("strike, instrument_type AS inst_type, lot_size AS lot, ")
            .append("exchange_token, tradingsymbol, instrument_token, exchange ")
            .append("FROM order_data WHERE IN (:tradingSymbols) ")
            .append(" Limit 1;")
            .toString();

    private static final String GET_ORDER_DATA_BY_TRADING_SYMBOL_ALL_V2 = new StringBuilder()
            .append("SELECT DISTINCT expiry AS expiry_key, ")
            .append("CONCAT(TO_CHAR(expiry, 'DD Mon'), '(', expiry - CURRENT_DATE, ' days)') AS expiry_value, ")
            .append("O_CHAR(expiry, 'DD Mon') AS exp,")
            .append("strike, instrument_type AS inst_type, lot_size AS lot, ")
            .append("exchange_token, tradingsymbol, instrument_token, exchange ")
            .append("FROM order_data WHERE IN (:tradingSymbols) ")
            .append(" Limit 1;")
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

    public PositionData setOrderDataByTradingSymbol(String tradingsymbol, PositionData positionData) {
        try {
            return jdbcTemplate.queryForObject(GET_ORDER_DATA_BY_TRADING_SYMBOL,
                    new Object[]{tradingsymbol}, (rs, rowNum) -> {
                        positionData.setExchangeToken(rs.getLong("exchange_token"));
                        positionData.setInstrumentToken(rs.getString("instrument_token"));
                        positionData.setTradingSymbol(rs.getString("tradingsymbol"));
                        positionData.setExpiry(rs.getString("expiry_key"));
                        positionData.setExpiryValue(rs.getString("expiry_value"));
                        positionData.setStrike(rs.getObject("strike") != null ? rs.getInt("strike") : 0); // Handle null safely
                        positionData.setExchange(rs.getString("exchange"));
                        return positionData;
                    });
        } catch (EmptyResultDataAccessException e) {
            logger.warn("No order data found for trading symbol: {}", tradingsymbol);
            return null; // Or return a default OrderData object if needed
        } catch (DataAccessException e) {
            logger.error("Database error while fetching order data for trading symbol: {}", tradingsymbol, e);
            throw new RuntimeException("Failed to fetch order data", e);
        }
    }


    public void updatePositionDataFromDB(List<PositionData> positionDataList) {
        if (positionDataList == null || positionDataList.isEmpty()) {
            logger.info("No positions to update from DB.");
            return;
        }

        List<String> tradingSymbols = positionDataList.stream()
                .map(PositionData::getTradingSymbol)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tradingSymbols.isEmpty()) {
            logger.info("No valid trading symbols found.");
            return;
        }

        Map<String, Object> params = Collections.singletonMap("symbols", tradingSymbols);
        List<Map<String, Object>> orderDataList = jdbcTemplate.queryForList(GET_ORDER_DATA_BY_TRADING_SYMBOL_ALL, params);

        // Create a lookup map for quick access
        Map<String, Map<String, Object>> orderDataMap = orderDataList.stream()
                .collect(Collectors.toMap(row -> (String) row.get("tradingsymbol"), row -> row));

        // Update positionData objects with DB values
        positionDataList.forEach(positionData -> {
            Map<String, Object> row = orderDataMap.get(positionData.getTradingSymbol());
            if (row != null) {
                positionData.setExchangeToken((Long) row.get("exchange_token"));
                positionData.setInstrumentToken((String) row.get("instrument_token"));
                positionData.setExpiry((String) row.get("expiry_key"));
                positionData.setExpiryValue((String) row.get("expiry_value"));
                positionData.setStrike(row.get("strike") != null ? ((Number) row.get("strike")).intValue() : 0);
                positionData.setExchange((String) row.get("exchange"));
            } else {
                logger.warn("No matching order data found for trading symbol: {}", positionData.getTradingSymbol());
            }
        });
    }


    public void updatePositionDataFromDBV2(List<PositionDataV2> positionDataList) {
        if (positionDataList == null || positionDataList.isEmpty()) {
            logger.info("No positions to update from DB.");
            return;
        }

        List<String> tradingSymbols = positionDataList.stream()
                .map(PositionDataV2::getTradingSymbol)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tradingSymbols.isEmpty()) {
            logger.info("No valid trading symbols found.");
            return;
        }

        Map<String, Object> params = Collections.singletonMap("symbols", tradingSymbols);
        List<Map<String, Object>> orderDataList = jdbcTemplate.queryForList(GET_ORDER_DATA_BY_TRADING_SYMBOL_ALL_V2, params);

        // Create a lookup map for quick access
        Map<String, Map<String, Object>> orderDataMap = orderDataList.stream()
                .collect(Collectors.toMap(row -> (String) row.get("tradingsymbol"), row -> row));

        // Update positionData objects with DB values
        positionDataList.forEach(positionData -> {
            Map<String, Object> row = orderDataMap.get(positionData.getTradingSymbol());
            if (row != null) {
                positionData.setInstrumentToken((String) row.get("instrument_token"));
                positionData.setExpiry((String) row.get("expiry_key"));
                positionData.setExpiryValue((String) row.get("expiry_value"));
                positionData.setExp((String) row.get("exp"));
                positionData.setStrike(row.get("strike") != null ? ((Number) row.get("strike")).intValue() : 0);
            } else {
                logger.warn("No matching order data found for trading symbol: {}", positionData.getTradingSymbol());
            }
        });
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
    


    public List<Broker> getALLTokenBrokers() {
        StringBuilder query = new StringBuilder()
                .append("SELECT b.broker_name, b.account, ")
                .append("TO_CHAR(b.updated_date, 'DD-Mon-YYYY HH12:MI AM') AS updated_date, ")
                .append("CASE WHEN b.updated_date::DATE = CURRENT_DATE THEN 'Active' ELSE 'Expired' END AS token_status ")
                .append("FROM brokers b WHERE b.is_active = TRUE ORDER BY b.account;");

        return jdbcTemplate.query(query.toString(), new RowMapper<Broker>() {
            @Override
            public Broker mapRow(ResultSet rs, int rowNum) throws SQLException {
                Broker broker = new Broker();
                broker.setBrokerName(rs.getString("broker_name"));
                broker.setAccount(rs.getString("account"));
                broker.setUpdatedDate(rs.getString("updated_date"));
                broker.setStatus(rs.getString("token_status"));
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
    public List<Broker> FindByBrokerId(int broker_id) {
        String query = "SELECT * FROM brokers WHERE is_active = TRUE and broker_id="+broker_id;

        return jdbcTemplate.query(query, new RowMapper<Broker>() {
            @Override
            public Broker mapRow(ResultSet rs, int rowNum) throws SQLException {
                Broker broker = new Broker();
                broker.setBrokerId(rs.getInt("broker_id"));
                broker.setBrokerName(rs.getString("broker_name"));
                broker.setBrokerName(rs.getString("display_name"));
                broker.setAccessToken(rs.getString("access_token"));
                broker.setApiKey(rs.getString("api_key"));
                return broker;
            }
        });
    }
}


