package admin.ui.connector.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

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
}




