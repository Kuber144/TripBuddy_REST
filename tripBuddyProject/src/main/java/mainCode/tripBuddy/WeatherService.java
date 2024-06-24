package mainCode.tripBuddy;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

public class WeatherService {

	private static final String API_BASE_URL = "https://api.tomorrow.io/v4/weather/realtime";
	private static final String API_KEY = "WRNkFYseImPvNcto7dKIwWczcwgbQSGM";
	private static final String TIME_API_URL = "https://timeapi.io/api/Time/current/coordinate";

	private static final double TEMPERATURE_THRESHOLD = 25; // Set the temperature threshold
	private static final double WIND_SPEED_THRESHOLD = 10; // Set the wind speed threshold

	public static WeatherInfo getWeatherInfo(String location) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8.toString());

		String url = String.format("%s?location=%s&units=metric&apikey=%s", API_BASE_URL, encodedLocation, API_KEY);

		try {
			String response = restTemplate.getForObject(url, String.class);
			System.out.println("Response from API: " + response); // Log response for debugging

			// Check if response is empty
			if (response == null || response.isEmpty()) {
				throw new Exception("Empty response received from API");
			}

			JSONObject jsonResponse = new JSONObject(response);

			// PARSING JSON RESPONSE
			JSONObject data = jsonResponse.getJSONObject("data");
			JSONObject values = data.getJSONObject("values");

			String time = data.getString("time");
			double temperature = values.getDouble("temperature");
			double humidity = values.getDouble("humidity");
			double windSpeed = values.getDouble("windSpeed");
			double windDirection = values.getDouble("windDirection");

			// Extract latitude and longitude
			JSONObject locationObj = jsonResponse.getJSONObject("location");
			float latitude = locationObj.getFloat("lat");
			float longitude = locationObj.getFloat("lon");

			// Fetch timezone from the Time API
			double timeZone = getTimeZone(latitude, longitude);

			// Generate description
			String description = generateDescription(temperature, windSpeed);

			// Construct WeatherInfo object
			WeatherInfo weatherInfo = new WeatherInfo();
			weatherInfo.setlat(latitude);
			weatherInfo.setlon(longitude);
			weatherInfo.setLocation(location);
			weatherInfo.setTimezone(timeZone);
			weatherInfo.setTime(time);
			weatherInfo.setTemperature(temperature);
			weatherInfo.setHumidity(humidity);
			weatherInfo.setWindSpeed(windSpeed);
			weatherInfo.setWindDirection(windDirection);
			weatherInfo.setDescription(description);

			return weatherInfo;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error fetching weather data from API", e);
		}
	}

	private static double getTimeZone(float latitude, float longitude) throws Exception {
		// Fetch timezone from the Time API
		String apiUrl = TIME_API_URL + "?latitude=" + latitude + "&longitude=" + longitude;
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.getForObject(apiUrl, String.class);
		JSONObject jsonResponse = new JSONObject(response);
		String timeZoneId = jsonResponse.getString("timeZone");

		// Get the ZoneId object for the timezone
		ZoneId zoneId = ZoneId.of(timeZoneId);

		// Get the offset in hours from UTC
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return zoneId.getRules().getOffset(now.toInstant()).getTotalSeconds() / 3600.0;
	}

	private static String generateDescription(double temperature, double windSpeed) {
		StringBuilder descriptionBuilder = new StringBuilder();
		// Construct description based on temperature and wind speed
		if (temperature > TEMPERATURE_THRESHOLD) {
			descriptionBuilder.append("Weather is hot. ");
		} else {
			descriptionBuilder.append("Weather is cold. ");
		}
		if (windSpeed > WIND_SPEED_THRESHOLD) {
			descriptionBuilder.append("High wind speeds. ");
		} else {
			descriptionBuilder.append("Low wind speeds. ");
		}
		// Return the constructed description
		return descriptionBuilder.toString();
	}
}
