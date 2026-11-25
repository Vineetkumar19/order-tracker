package dev.ordertracker.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<OrderService.Order> create(@Valid @RequestBody CreateOrder request) {
        var order = orders.create(request.sku(), request.quantity());
        return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderService.Order> find(@PathVariable UUID id) {
        return ResponseEntity.of(orders.find(id));
    }

    public record CreateOrder(
            @NotBlank @Size(max = 64) String sku,
            @NotNull @Min(1) Integer quantity) {}
}
