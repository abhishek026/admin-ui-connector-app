package admin.ui.connector.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionData {
    private long exchangeToken;
    private String instrumentToken;
    private String tradingSymbol;
    private String expiry;
    private String expiryValue;
    private int strike;
    private double stopLoss;
    private String exchange;
    private String orderType = "MARKET";
    private String transactionType = "";


}