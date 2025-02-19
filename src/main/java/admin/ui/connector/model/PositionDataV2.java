package admin.ui.connector.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionDataV2 {
    private String instrumentToken;
    private String tradingSymbol;
    private String expiry;
    private String expiryValue;
    private int strike;
    private String exp;


}