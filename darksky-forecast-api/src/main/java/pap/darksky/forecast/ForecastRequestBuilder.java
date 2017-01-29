package pap.darksky.forecast;

import java.net.MalformedURLException;
import java.net.URL;
import static pap.darkysky.forecast.util.Assert.nonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Can be used to add optional RequestParamters.
 *
 * @author Puls
 */
public class ForecastRequestBuilder {

    private static final String URL = " https://api.darksky.net/forecast/##key##/##latitude##,##longitude##";
    private final List<Block> exclusion = new ArrayList<>();
    private Language language = Language.de;
    private Units units = Units.si;
    private String overrideUrl;
    private boolean extendHourly;
    private GeoCoordinates geoCoordinates;
    private APIKey apiKey;

    /**
     * @param apiKey The APIKey to authenticate with the DarkSky API.
     * @return This for fluent API.
     */
    public ForecastRequestBuilder key(APIKey apiKey) {
        nonNull("APIKey cannot be null.", apiKey);

        this.apiKey = apiKey;
        return this;
    }

    /**
     * @param geoCoordinates Geocordinates identifying the location for which the weather forecast is requested.
     * @return This for fluent API.
     */
    public ForecastRequestBuilder location(GeoCoordinates geoCoordinates) {
        nonNull("GeoCoordinates cannot be null.", geoCoordinates);

        this.geoCoordinates = geoCoordinates;
        return this;
    }

    /**
     * @param url Override the default DarksSky API Url. The URL must contain the following patterns for the key and
     * gelocation:<br>
     *
     * ##key## ##latitude## ##longitude##
     *
     * @return This for fluent API.
     */
    public ForecastRequestBuilder url(String url) {
        nonNull("url cannot be null.", url);

        this.overrideUrl = url;
        return this;
    }

    /**
     * @param language The Language which is used in the Forecast response.
     * @return This for fluent API.
     */
    public ForecastRequestBuilder language(Language language) {
        nonNull("language cannot be null.", language);

        this.language = language;
        return this;
    }

    /**
     * When set, return hour-by-hour data for the next 168 hours, instead of the next 48. When using this option, we strongly
     * recommend enabling HTTP compression.
     *
     * @return This for fluent API.
     */
    public ForecastRequestBuilder extendHourly() {
        this.extendHourly = true;
        return this;
    }

    /**
     * @param block The Blocks which shall be excluded from the response to save data / latency. This method can be called
     * multiple times and the exclusion will add up.
     * @return This for fluent API.
     */
    public ForecastRequestBuilder exdclude(Block... block) {
        this.exclusion.addAll(Arrays.asList(block));
        return this;
    }

    /**
     * @param units The Units which are used in the Forecast response.
     * @return This for fluent API.
     */
    public ForecastRequestBuilder units(Units units) {
        nonNull("units cannot be null.", units);

        this.units = units;
        return this;
    }

    /**
     * @return The Request with the given parameters set.
     */
    public ForecastRequest build() {
        try {
            return new ForecastRequest(getUrl());
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Cannot create Forecast Request. The URL is invalid!", ex);
        }
    }

    /**
     * @return The Url build from the given paramters.
     * @throws MalformedURLException
     */
    private URL getUrl() throws MalformedURLException {
        nonNull("The ApIkey must be set. Please call the corresponding method.", apiKey);
        nonNull("The Gelocation must be set. Please call the corresponding method.", geoCoordinates);

        String forecastUrlString = URL;
        if (overrideUrl != null) {
            forecastUrlString = overrideUrl;
        }
        forecastUrlString = forecastUrlString.replaceAll("##key##", apiKey.value())
                .replaceAll("##latitude##", String.valueOf(geoCoordinates.latitude().value()))
                .replaceAll("##longitude##", String.valueOf(geoCoordinates.longitude().value()))
                + requuestParamtersAsString();

        return new URL(forecastUrlString);
    }

    /**
     * @return The RequestParamters as String formatted so that they can be added to the vase forecast url.
     */
    private String requuestParamtersAsString() {
        StringBuilder paramBuilder = new StringBuilder("?");
        if (language != null) {
            paramBuilder.append(RequestParmaterType.lang.name());
            paramBuilder.append("=");
            paramBuilder.append(language.name());
            paramBuilder.append("&");
        }
        if (units != null) {
            paramBuilder.append(RequestParmaterType.units.name());
            paramBuilder.append("=");
            paramBuilder.append(units.name());
            paramBuilder.append("&");
        }
        if (!exclusion.isEmpty()) {
            paramBuilder.append(RequestParmaterType.exclude.name());
            paramBuilder.append("=");
            StringJoiner joiner = new StringJoiner(",");
            exclusion.stream().forEach(s -> joiner.add(s.name()));
            paramBuilder.append(joiner.toString());
            paramBuilder.append("&");
        }
        if (extendHourly) {
            paramBuilder.append(RequestParmaterType.extend.name());
            paramBuilder.append("=");
            paramBuilder.append(Block.hourly.name());
            paramBuilder.append("&");
        }
        return paramBuilder.substring(0, paramBuilder.length() - 1);
    }

    /**
     * The available Languages in which the forecast response is translated.
     */
    public enum Language {
        de,
        en
    }

    /**
     * The blocks of the forecast response which can be excluded.
     */
    public enum Block {
        currently,
        minutely,
        hourly,
        daily,
        alerts,
        flags,
    }

    public enum Units {
        /**
         * automatically select units based on geographic location
         */
        auto,
        /**
         * same as si, except that windSpeed is in kilometers per hour
         */
        ca,
        /**
         * SI units are as follows: summary: Any summaries containing temperature or snow accumulation units will have their
         * values in degrees Celsius or in centimeters (respectively). nearestStormDistance: Kilometers.<br>
         * precipIntensity: Millimeters per hour.<br>
         * precipIntensityMax: Millimeters per hour.<br>
         * precipAccumulation: Centimeters.<br>
         * temperature: Degrees Celsius. <br>
         * temperatureMin: Degrees Celsius. <br>
         * temperatureMax: Degrees Celsius. <br>
         * apparentTemperature: Degrees Celsius. <br>
         * dewPoint: Degrees Celsius. <br>
         * windSpeed: Meters per second. <br>
         * pressure: Hectopascals.<br>
         * visibility: Kilometers.<br>
         */
        si,
        /**
         * same as si, except that nearestStormDistance and visibility are in miles and windSpeed is in miles per hour
         */
        uk2,
        /**
         * Imperial units (the default)
         */
        us
    }

    private enum RequestParmaterType {
        exclude,
        extend,
        lang,
        units
    }
}
