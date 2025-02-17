package admin.ui.connector.Controller;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import admin.ui.connector.ticker.ZerodhaTickerService;
import admin.ui.connector.utills.ResponseUtil;

@RestController
@RequestMapping("/zerodha")
public class ZerodhaController {

    private final ZerodhaTickerService tickerService;

    public ZerodhaController(ZerodhaTickerService tickerService) {
        this.tickerService = tickerService;
    }

    @PostMapping("/start/{accountId}")
    public ResponseEntity<?> startTicker(@PathVariable String accountId, @RequestParam String apiKey, @RequestParam String accessToken, @RequestBody ArrayList<Long> tokens) {
        tickerService.startTickerForAccount(accountId, apiKey, accessToken, tokens);
		return ResponseUtil.buildResponse(HttpStatus.CREATED, String.format("Ticker started successfully for account: %s!", accountId));
    }

    @PostMapping("/stop/{accountId}")
    public ResponseEntity<?>  stopTicker(@PathVariable String accountId) {
        tickerService.stopTickerForAccount(accountId);
		return ResponseUtil.buildResponse(HttpStatus.CREATED, String.format("Ticker stop successfully for account: %s!", accountId));
    }
}
