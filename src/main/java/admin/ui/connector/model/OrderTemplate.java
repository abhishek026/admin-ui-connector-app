package admin.ui.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderTemplate {
    private long id;
    private Long exchangeToken;
    private Long instrumentToken;
    private String tradingSymbol;
    private LocalDate expiry;
    private String expiryValue;
    private BigDecimal strike;
    private String instType;
    private int lotSize;
    private BigDecimal stopLoss;
    private int broker;
    private int lots;
    private int qty;
    private String exchange;
    private String orderType;
    private String transactionType;
    private String templateName;
    private LocalDateTime createDate=LocalDateTime.now();
    private LocalDateTime modifiedDate=LocalDateTime.now();
    private List<Long> brokers=new ArrayList<>();
    @JsonIgnore
    private Boolean active;

}
