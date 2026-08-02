package com.example.aidemo.businesslogic;

import com.example.aidemo.common.ChannelStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ch06")
public class Ch06BusinessLogicController {

    private final TicketClassificationService classificationService;

    public Ch06BusinessLogicController(TicketClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping("/status")
    public ChannelStatusResponse status() {
        return new ChannelStatusResponse("CH.06", "businesslogic", "KI-gestützte Business-Logik bereit");
    }

    @PostMapping("/classify")
    public TicketClassificationResponse classify(@RequestBody TicketClassificationRequest request) {
        TicketClassification classification = classificationService.classify(request.ticketText());
        return new TicketClassificationResponse("CH.06", "businesslogic", request.ticketText(), classification);
    }
}
