package net.biswajit.journalApp.service;

import net.biswajit.journalApp.cache.AppCache;
import net.biswajit.journalApp.constant.PlaceHolder;
import net.biswajit.journalApp.api.response.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String api_Key;

    @Autowired
    private AppCache appCache;

    @Autowired
    private RestTemplate restTemplate;

    public WeatherResponse getWeather(String city){
        String finalLink = appCache.appCache.get(AppCache.keys.WEATHER_API.toString()).replace(PlaceHolder.CITY, city).replace(PlaceHolder.API_KEY, api_Key);
        ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalLink, HttpMethod.GET, null, WeatherResponse.class);
        WeatherResponse body = response.getBody();
        return body;
    }
}
