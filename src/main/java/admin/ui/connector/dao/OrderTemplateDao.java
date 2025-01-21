package admin.ui.connector.dao;

import admin.ui.connector.model.OrderTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
public class OrderTemplateDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "INSERT INTO order_template (exchange_token, instrument_token, trading_symbol, expiry, expiry_value, " +
            "strike, inst_type, lot_size, stop_loss, broker, lots, qty, exchange, order_type, transaction_type, " +
            "template_name, create_date, modified_date, active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String DELETE_SQL = "DELETE FROM order_template WHERE template_name = ?";
    private static final String SELECT_BY_TEMPLATE_NAME_SQL = "SELECT * FROM order_template WHERE active = TRUE and template_name = ? order by id";
    private static final String SELECT_ALL_TEMPLATES_SQL = "SELECT * FROM order_template WHERE active = TRUE";

    public void saveOrderTemplates(List<OrderTemplate> orderTemplates) {
        orderTemplates.forEach(order -> jdbcTemplate.update(DELETE_SQL, order.getTemplateName()));
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrderTemplate order = orderTemplates.get(i);
                setOrderTemplateValues(ps, order);
                ps.setTimestamp(17, new Timestamp(System.currentTimeMillis()));
                ps.setTimestamp(18, new Timestamp(System.currentTimeMillis()));
                ps.setBoolean(19, true);
            }

            @Override
            public int getBatchSize() {
                return orderTemplates.size();
            }
        });
    }

    public void updateOrderTemplates(List<OrderTemplate> orderTemplates) {
        orderTemplates.forEach(order -> jdbcTemplate.update(DELETE_SQL, order.getTemplateName()));
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrderTemplate order = orderTemplates.get(i);
                setOrderTemplateValues(ps, order);
                ps.setTimestamp(17, new Timestamp(System.currentTimeMillis()));
                ps.setTimestamp(18, new Timestamp(System.currentTimeMillis()));
                ps.setBoolean(19, true);
            }

            @Override
            public int getBatchSize() {
                return orderTemplates.size();
            }
        });
    }

    public List<OrderTemplate> findTemplateByName(String templateName) {
        return jdbcTemplate.query(SELECT_BY_TEMPLATE_NAME_SQL, new Object[]{templateName}, new OrderTemplateRowMapper());
    }

    public List<OrderTemplate> getAllTemplates() {
        return jdbcTemplate.query(SELECT_ALL_TEMPLATES_SQL, new OrderTemplateRowMapper());
    }

    public void deleteByTemplateName(String templateName) {
        jdbcTemplate.update(DELETE_SQL, templateName);
    }

    private void setOrderTemplateValues(PreparedStatement ps, OrderTemplate order) throws SQLException {
        ps.setLong(1, order.getExchangeToken());
        ps.setLong(2, order.getInstrumentToken());
        ps.setString(3, order.getTradingSymbol());
        ps.setDate(4, Date.valueOf(order.getExpiry()));
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
        ps.setString(16, order.getTemplateName());
    }

}

class OrderTemplateRowMapper implements RowMapper<OrderTemplate> {
    @Override
    public OrderTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
        OrderTemplate order = new OrderTemplate();
        order.setId(rs.getLong("id"));
        order.setExchangeToken(rs.getLong("exchange_token"));
        order.setInstrumentToken(rs.getLong("instrument_token"));
        order.setTradingSymbol(rs.getString("trading_symbol"));
        order.setExpiry(rs.getDate("expiry").toLocalDate());
        order.setExpiryValue(rs.getString("expiry_value"));
        order.setStrike(rs.getBigDecimal("strike"));
        order.setInstType(rs.getString("inst_type"));
        order.setLotSize(rs.getInt("lot_size"));
        order.setStopLoss(rs.getBigDecimal("stop_loss"));
        order.setBroker(rs.getInt("broker"));
        order.setLots(rs.getInt("lots"));
        order.setQty(rs.getInt("qty"));
        order.setExchange(rs.getString("exchange"));
        order.setOrderType(rs.getString("order_type"));
        order.setTransactionType(rs.getString("transaction_type"));
        order.setTemplateName(rs.getString("template_name"));
        order.setCreateDate(rs.getTimestamp("create_date").toLocalDateTime());
        order.setModifiedDate(rs.getTimestamp("modified_date").toLocalDateTime());
        order.setActive(rs.getBoolean("active"));
        return order;
    }
}
