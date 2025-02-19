package admin.ui.connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdminUiConnectorAppApplication {

    public static void main(String[] args) {


        SpringApplication.run(AdminUiConnectorAppApplication.class, args);
        System.out.println("Admin UI Connector App Application Started");

    }

}
