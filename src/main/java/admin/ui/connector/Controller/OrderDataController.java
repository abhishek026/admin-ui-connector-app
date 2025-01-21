package admin.ui.connector.Controller;

import admin.ui.connector.business.OrderDataService;
import admin.ui.connector.model.OrderPlacement;
import admin.ui.connector.model.OrderTemplate;
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


    @GetMapping("/expiry-dates")
    public ResponseEntity<Map<String, String>> getExpiryDates() {
        Map<String, String> expiryDates = orderDataService.getExpiryDates();
        return ResponseEntity.ok(expiryDates);
    }

    @GetMapping("/expiry-data")
    public ResponseEntity<OrderPlacement> getOrderData(@RequestParam("expiry") String expiry) {
        OrderPlacement orderPlacement = orderDataService.getOrderDataByExpiry(expiry);
        return ResponseEntity.ok(orderPlacement);
    }

    @PostMapping("/save-order-template")
    public ResponseEntity<String> saveOrderTemplate(@RequestBody List<OrderTemplate> orderTemplates) {
        orderDataService.saveOrderTemplate(orderTemplates);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Template(s) created successfully!");
    }

    @PatchMapping("/update-order-template")
    public ResponseEntity<String> updateOrderTemplate(@RequestBody List<OrderTemplate> orderTemplates) {
        try {
            orderDataService.updateOrderTemplates(orderTemplates);
            return ResponseEntity.ok("Template(s) modified successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/getAllTemplates")
    public ResponseEntity<Map<String, List<OrderTemplate>>> getAllTemplates() {
        Map<String, List<OrderTemplate>> templates = orderDataService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/find-order-template")
    public ResponseEntity<Map<String, List<OrderTemplate>>> findTemplateByName(@RequestParam String templateName) {
        Map<String, List<OrderTemplate>> templates = orderDataService.findTemplateByName(templateName);
        return ResponseEntity.ok(templates);
    }

    @DeleteMapping("/deleteByTemplateName")
    public ResponseEntity<String> deleteByTemplateName(@RequestParam String templateName) {
        try {
            orderDataService.deleteByTemplateName(templateName);
            return ResponseEntity.ok("Template(s) with name '" + templateName + "' deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting template(s): " + e.getMessage());
        }
    }
}
