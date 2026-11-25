package dev.ordertracker.inventory;

import java.util.Map;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {
    private static final Map<String, Integer> STOCK = Map.of("NOTEBOOK", 5, "PEN", 10);

    @GetMapping("/inventory/{sku}")
    public Stock stock(@PathVariable String sku) {
        String key = sku.toUpperCase(Locale.ROOT);
        return new Stock(key, STOCK.getOrDefault(key, 0));
    }

    public record Stock(String sku, int available) {}
}
