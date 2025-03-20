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
    private RedisService redisService;

    @Autowired
    private RestTemplate restTemplate;

    public WeatherResponse getWeather(String city){
        String key = "weather_of_"+city;
        WeatherResponse weatherResponse = redisService.get(key, WeatherResponse.class);

        if(weatherResponse != null){
            return weatherResponse;
        }else{
            String finalLink = appCache.appCache.get(AppCache.keys.WEATHER_API.toString())
                    .replace(PlaceHolder.CITY, city)
                    .replace(PlaceHolder.API_KEY, api_Key);

            ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalLink, HttpMethod.GET, null, WeatherResponse.class);
            WeatherResponse body =  response.getBody();

            if(body != null){
                redisService.set(key, body, 300L);
            }
            return body;
        }
    }

    /**
     * Capitalizes the first letter of each word in the city name
     */
    public String formatCityName(String city) {

        if (city == null || city.isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        String[] words = city.toLowerCase().split("\\s+");
        StringBuilder formattedCity = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedCity.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return formattedCity.toString().trim();
    }
}
