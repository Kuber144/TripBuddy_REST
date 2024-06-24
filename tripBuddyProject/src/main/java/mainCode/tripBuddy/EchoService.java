package mainCode.tripBuddy;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoService {

	@GetMapping("/echo")
	public String echoMessage(@RequestParam("sourceCity") String sourceCity,
			@RequestParam("destinationCity") String destinationCity) {
		try {
			// Fetch weather information for source and destination cities
			System.out.println("Backend api get call invoked");
			System.out.println("Fetching weather details for source and destination");
			WeatherInfo sourceWeather = WeatherService.getWeatherInfo(sourceCity);
			WeatherInfo destinationWeather = WeatherService.getWeatherInfo(destinationCity);
			System.out.println("Weather fetched");

			// Calculate the time zone difference between both
			System.out.println("Calculating time zone difference");
			double timeZoneDifference = Math.abs(sourceWeather.getTimezone() - destinationWeather.getTimezone());
			System.out.println("Time zone difference calculated");

			// Fetch the interesting activities for both
			System.out.println("Fetching interesting activities for the source and destination");
			Set<String> interestingActivitiesSource = GetLocations.getUniqueKinds(sourceWeather.getlat(),
					sourceWeather.getlon());
			Set<String> interestingActivitiesDestination = GetLocations.getUniqueKinds(destinationWeather.getlat(),
					destinationWeather.getlon());
			System.out.println("All details fetched creating a json and sending response");

			// Create and return EchoMessage object with trip details
			EchoMessage echoMessage = new EchoMessage(sourceCity, destinationCity, sourceWeather, destinationWeather,
					timeZoneDifference, interestingActivitiesSource, interestingActivitiesDestination);
			return echoMessage.toJsonString();
		} catch (Exception e) {
			// Handle the exception, e.g., log it or return an error message
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
			return "An error occurred. Please try again with the correct details";
		}
	}
}
