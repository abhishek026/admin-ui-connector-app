package admin.ui.connector.model;

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

    public long getExchangeToken() {
        return exchangeToken;
    }

    public void setExchangeToken(long exchangeToken) {
        this.exchangeToken = exchangeToken;
    }

    public long getInstrumentToken() {
        return instrumentToken;
    }

    public void setInstrumentToken(long instrumentToken) {
        this.instrumentToken = instrumentToken;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public int getLots() {
        return lots;
    }

    public void setLots(int lots) {
        this.lots = lots;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getExpiryValue() {
        return expiryValue;
    }

    public void setExpiryValue(String expiryValue) {
        this.expiryValue = expiryValue;
    }

    public int getStrike() {
        return strike;
    }

    public void setStrike(int strike) {
        this.strike = strike;
    }

    public String getInstType() {
        return instType;
    }

    public void setInstType(String instType) {
        this.instType = instType;
    }

    public int getLotSize() {
        return lotSize;
    }

    public void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public void setBroker(int broker) {
        this.broker = broker;
    }

    public int getBroker() {
        return broker;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getExchange() {
        return exchange;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
