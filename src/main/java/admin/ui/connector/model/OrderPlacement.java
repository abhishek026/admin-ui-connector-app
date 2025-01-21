package admin.ui.connector.model;

import java.util.List;

public class OrderPlacement {
    private List<OrderData> orderDataList;
    private List<Broker> brokers;

    public void setBrokers(List<Broker> brokers) {
        this.brokers = brokers;
    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    public List<OrderData> getOrderDataList() {
        return orderDataList;
    }

    public void setOrderDataList(List<OrderData> orderDataList) {
        this.orderDataList = orderDataList;
    }
}
