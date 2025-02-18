package admin.ui.connector.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import admin.ui.connector.model.Broker;
import admin.ui.connector.model.BrokerPositions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.zerodhatech.kiteconnect.KiteConnect;

@Repository
public class BrokerDataDao {

    private final Logger logger = LogManager.getLogger(BrokerDataDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;


    @SuppressWarnings("deprecation")
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


    public List<BrokerPositions> getActiveTokenBrokers() {
        StringBuilder query = new StringBuilder()
                .append("SELECT b.broker_id, b.broker_name, b.account, b.api_key, ")
                .append("b.user_id, b.public_token, b.access_token, ")
                .append("TO_CHAR(b.updated_date, 'DD-Mon-YYYY HH12:MI AM') AS updated_date ")
                .append("FROM brokers b ")
                .append("WHERE b.is_active = TRUE AND b.updated_date::DATE = CURRENT_DATE ") // Only active brokers
                .append("ORDER BY b.account;");

        return jdbcTemplate.query(query.toString(), new RowMapper<BrokerPositions>() {
            @Override
            public BrokerPositions mapRow(ResultSet rs, int rowNum) throws SQLException {
                BrokerPositions broker = new BrokerPositions();
                broker.setBrokerId(rs.getInt("broker_id"));
                broker.setBrokerName(rs.getString("broker_name"));
                broker.setAccount(rs.getString("account"));
                broker.setApiKey(rs.getString("api_key"));
                broker.setUserId(rs.getString("user_id"));
                broker.setPublicToken(rs.getString("public_token"));
                broker.setAccessToken(rs.getString("access_token"));
                return broker;
            }
        });
    }

}




