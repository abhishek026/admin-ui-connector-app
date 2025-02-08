package admin.ui.connector.kiteconnect;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KiteCredentials {
    private String apiKey;
    private String userId;
    private String requestToken;
    private String apiSecret;
    private Long brokerId;

}