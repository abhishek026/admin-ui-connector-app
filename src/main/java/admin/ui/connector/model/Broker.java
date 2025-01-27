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
    private String brokerUrl;
    @JsonIgnore
    private String brokerUsername;
    @JsonIgnore
    private String brokerPassword;
    @JsonIgnore
    private String accessToken;
    private boolean isActive;
}
