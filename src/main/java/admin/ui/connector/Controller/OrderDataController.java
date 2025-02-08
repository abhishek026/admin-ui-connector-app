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
public class OrderDataController {

    @Autowired
    private OrderDataService orderDataService;

    @Autowired
    private KiteClientBusiness kiteClientBusiness;


    @GetMapping("/expiry-dates")
    public ResponseEntity<?> getExpiryDates() {
        Map<String, String> expiryDates = orderDataService.getExpiryDates();
        return ResponseUtil.buildResponse(expiryDates);
    }

    @GetMapping("/expiry-data")
    public ResponseEntity<?> getOrderData(@RequestParam("expiry") String expiry) {
        OrderPlacement orderPlacement = orderDataService.getOrderDataByExpiry(expiry);
        return ResponseUtil.buildResponse(orderPlacement);
    }

    @PostMapping("/save-order-template")
    public ResponseEntity<?> saveOrderTemplate(@RequestBody List<OrderTemplate> orderTemplates) {
        orderDataService.saveOrderTemplate(orderTemplates);
        return ResponseUtil.buildResponse(HttpStatus.CREATED,"Template(s) created successfully!");
    }

    @PatchMapping("/update-order-template")
    public ResponseEntity<?> updateOrderTemplate(@RequestBody List<OrderTemplate> orderTemplates) {
        try {
            orderDataService.updateOrderTemplates(orderTemplates);
            return ResponseUtil.buildResponse("Template(s) modified successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/getAllTemplates")
    public ResponseEntity<?> getAllTemplates() {
        Map<String, List<OrderTemplate>> templates = orderDataService.getAllTemplates();
        return ResponseUtil.buildResponse(templates);
    }

    @GetMapping("/find-order-template")
    public ResponseEntity<?> findTemplateByName(@RequestParam String templateName) {
        Map<String, List<OrderTemplate>> templates = orderDataService.findTemplateByName(templateName);
        return ResponseUtil.buildResponse(templates);
    }

    @DeleteMapping("/deleteByTemplateName")
    public ResponseEntity<?> deleteByTemplateName(@RequestParam String templateName) {
        try {
            orderDataService.deleteByTemplateName(templateName);
            return ResponseUtil.buildResponse("Template(s) with name '" + templateName + "' deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting template(s): " + e.getMessage());
        }
    }

    @GetMapping("/get-broker-list")
    public ResponseEntity<?> getBrokerList() {
        List<Broker>  brokers= orderDataService.getBrokerList();
        return ResponseUtil.buildResponse(brokers);
    }

    @PostMapping("/generateToken")
    public ResponseEntity<?> generateToken(@RequestBody KiteCredentials credentials) {
       kiteClientBusiness.generateTokens(credentials);
        return ResponseUtil.buildResponse(HttpStatus.CREATED,"Token generated successfully!");
    }

    @GetMapping("/get-active-token-brokers")
    public List<Broker> getActiveBrokers() {
        return orderDataService.getActiveTokenBrokers();
    }
}
