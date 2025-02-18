package admin.ui.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BrokerPositions {
    private int brokerId;
    private String brokerName;
    private String displayName;
    private String account;
    private String publicToken;
    private String accessToken;
    private String status;
    private String userId;
    private String apiKey;
    private List<PositionData> positions;
}
