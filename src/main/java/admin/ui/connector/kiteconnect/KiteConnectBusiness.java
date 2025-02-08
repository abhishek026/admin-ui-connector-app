package admin.ui.connector.kiteconnect;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KiteConnectBusiness {

    private final Logger logger = LogManager.getLogger(KiteConnectBusiness.class);

    public KiteConnect generateTokens(KiteCredentials credentials) throws IOException, KiteException {
        KiteConnect kiteConnect = new KiteConnect(credentials.getApiKey());

        kiteConnect.setUserId(credentials.getUserId());

        kiteConnect.setSessionExpiryHook(() -> System.out.println("Session expired"));

        // Generate session and retrieve user details
        User user = kiteConnect.generateSession(credentials.getRequestToken(), credentials.getApiSecret());

        kiteConnect.setAccessToken(user.accessToken);
        kiteConnect.setPublicToken(user.publicToken);

        logger.info("Access Token: " + user.accessToken);
        logger.info("Public Token: " + user.publicToken);
        logger.info("Login successful. Access Token generated.");

        return kiteConnect;
    }

}
