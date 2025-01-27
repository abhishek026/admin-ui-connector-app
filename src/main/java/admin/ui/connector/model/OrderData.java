package admin.ui.connector.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderData {
    private long exchangeToken;
    private long instrumentToken;
    private String tradingSymbol;
    private String expiry;
    private String expiryValue;
    private int strike;
    private String instType;
    private int lotSize;
    private double stopLoss;
    private int broker;
    private int lots;
    private int qty;
    private String exchange;
    private String orderType = "MARKET";
    private String transactionType = "";
}
