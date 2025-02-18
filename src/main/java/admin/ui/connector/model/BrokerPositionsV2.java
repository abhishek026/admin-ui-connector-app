package admin.ui.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BrokerPositionsV2 {
    private int brokerId;
    private String brokerName;
    private String displayName;
    private String account;
    @JsonIgnore
    private String publicToken;
    @JsonIgnore
    private String accessToken;
    @JsonIgnore
    private String userId;
    @JsonIgnore
    private String apiKey;
    private List<PositionDataV2> positions;
}
