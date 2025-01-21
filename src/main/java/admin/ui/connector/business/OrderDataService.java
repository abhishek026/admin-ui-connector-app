package admin.ui.connector.business;

import admin.ui.connector.dao.OrderDataDao;
import admin.ui.connector.dao.OrderTemplateDao;
import admin.ui.connector.model.OrderPlacement;
import admin.ui.connector.model.OrderTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderDataService {

    @Autowired
    private OrderDataDao orderDataDao;
    @Autowired
    private OrderTemplateDao orderTemplateDao;


    public Map<String, String> getExpiryDates() {
        return orderDataDao.getExpiryDates();
    }

    public OrderPlacement getOrderDataByExpiry(String expiry) {
        OrderPlacement orderPlacement = new OrderPlacement();
        orderPlacement.setOrderDataList(orderDataDao.getOrderDataByExpiry(expiry));
        orderPlacement.setBrokers(orderDataDao.getActiveBrokers());
        return orderPlacement;
    }

    public void saveOrderTemplate(List<OrderTemplate> orderTemplates) {
        validateOrderTemplates(orderTemplates);
        orderTemplateDao.saveOrderTemplates(orderTemplates);
    }

    public void updateOrderTemplates(List<OrderTemplate> orderTemplates) {
        validateOrderTemplates(orderTemplates);
        orderTemplateDao.updateOrderTemplates(orderTemplates);
    }

    public Map<String, List<OrderTemplate>> getAllTemplates() {
        List<OrderTemplate> templates = orderTemplateDao.getAllTemplates();
        return groupTemplatesByName(templates);
    }

    public Map<String, List<OrderTemplate>> findTemplateByName(String templateName) {
        List<OrderTemplate> templates = orderTemplateDao.findTemplateByName(templateName);
        return groupTemplatesByName(templates);
    }

    public void deleteByTemplateName(String templateName) {
        if (templateName == null || templateName.isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        orderTemplateDao.deleteByTemplateName(templateName);
    }

    private void validateOrderTemplates(List<OrderTemplate> orderTemplates) {
        if (orderTemplates == null || orderTemplates.isEmpty()) {
            throw new IllegalArgumentException("Order templates cannot be null or empty");
        }
    }

    private Map<String, List<OrderTemplate>> groupTemplatesByName(List<OrderTemplate> templates) {
        return templates.stream()
                .collect(Collectors.groupingBy(OrderTemplate::getTemplateName));
    }
}
