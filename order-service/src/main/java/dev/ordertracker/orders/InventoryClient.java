package dev.ordertracker.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InventoryClient {
    private final RestClient http;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl) {
        var requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(1000);
        requests.setReadTimeout(2000);
        http = RestClient.builder().baseUrl(baseUrl).requestFactory(requests).build();
    }

    public int available(String sku) {
        try {
            var stock = http.get().uri("/inventory/{sku}", sku).retrieve().body(Stock.class);
            if (stock == null || stock.available() == null || stock.available() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid inventory response");
            }
            return stock.available();
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Inventory is unavailable", ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Inventory request failed", ex);
        }
    }

    public record Stock(Integer available) {}
}
