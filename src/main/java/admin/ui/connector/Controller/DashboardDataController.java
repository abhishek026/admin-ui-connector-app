package admin.ui.connector.Controller;

import admin.ui.connector.business.OrderDataService;
import admin.ui.connector.kiteconnect.KiteClientBusiness;
import admin.ui.connector.kiteconnect.KiteCredentials;
import admin.ui.connector.model.Broker;
import admin.ui.connector.model.OrderPlacement;
import admin.ui.connector.model.OrderTemplate;
import admin.ui.connector.utills.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardDataController {

	@Autowired
	private OrderDataService orderDataService;

	@Autowired
	private KiteClientBusiness kiteClientBusiness;

	@GetMapping("/get-broker-position-v2")
	public ResponseEntity<?> getAllBrokerPositionsV2() {
		return ResponseUtil.buildResponse(kiteClientBusiness.getAllBrokerPositionsV2());

	}
}
