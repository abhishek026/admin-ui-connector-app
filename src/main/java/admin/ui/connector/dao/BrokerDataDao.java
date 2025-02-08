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
import java.util.Optional;

@Repository
public class BrokerDataDao {

    private final Logger logger = LogManager.getLogger(BrokerDataDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;


    public KiteConnect getBrokerTokens(Long brokerId) {
        String query = "SELECT api_key, user_id, public_token, access_token FROM brokers WHERE broker_id = ?";

        return jdbcTemplate.query(query, new Object[]{brokerId}, new RowMapper<KiteConnect>() {
            @Override
            public KiteConnect mapRow(ResultSet rs, int rowNum) throws SQLException {
                KiteConnect kiteConnect = new KiteConnect(rs.getString("api_key"));
                kiteConnect.setUserId(rs.getString("user_id"));
                kiteConnect.setPublicToken(rs.getString("public_token"));
                kiteConnect.setAccessToken(rs.getString("access_token"));
                return kiteConnect;
            }
        }).stream().findFirst().get();
    }
}




