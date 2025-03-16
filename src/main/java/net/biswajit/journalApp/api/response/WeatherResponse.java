package net.biswajit.journalApp.api.response;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class WeatherResponse{

    private Current current;

    @Data
    public static class Current{
        private int temperature;
        private int feelslike;
    }
}



