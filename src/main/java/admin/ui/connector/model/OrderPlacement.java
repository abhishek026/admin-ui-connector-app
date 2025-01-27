package admin.ui.connector.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class OrderPlacement {
    private List<OrderData> orderDataList;
    private List<Broker> brokers;
}
