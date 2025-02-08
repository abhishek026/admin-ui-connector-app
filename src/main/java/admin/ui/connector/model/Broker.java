package admin.ui.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Broker {
    private int brokerId;
    private String brokerName;
    private String displayName;
    private String account;
    @JsonIgnore
    private String brokerUrl;
    @JsonIgnore
    private String publicToken;
    @JsonIgnore
    private String accessToken;
    @JsonIgnore
    private boolean isActive;
}
