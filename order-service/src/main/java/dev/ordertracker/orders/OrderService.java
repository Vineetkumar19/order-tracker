package dev.ordertracker.orders;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final InventoryClient inventory;
    private final JdbcTemplate db;

    public OrderService(InventoryClient inventory, JdbcTemplate db) {
        this.inventory = inventory;
        this.db = db;
    }

    public Order create(String sku, int quantity) {
        if (inventory.available(sku.strip()) < quantity) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Insufficient stock"
            );
        }

        Order order = new Order(UUID.randomUUID(), sku.strip(), quantity, Instant.now());

        // Save the order to the PostgreSQL table.
        db.update("""
            INSERT INTO orders (id, sku, quantity, created_at)
            VALUES (?, ?, ?, ?)
            """,
            order.id(),
            order.sku(),
            order.quantity(),
            order.createdAt().atOffset(ZoneOffset.UTC)
        );

        return order;
    }

    public Optional<Order> find(UUID id) {
        // Look up the order by its ID in PostgreSQL.
        return db.query("""
            SELECT id, sku, quantity, created_at
            FROM orders
            WHERE id = ?
            """,
            (rs, rowNum) -> new Order(
                rs.getObject("id", UUID.class),
                rs.getString("sku"),
                rs.getInt("quantity"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()
            ),
            id
        ).stream().findFirst();
    }

    public record Order(UUID id, String sku, int quantity, Instant createdAt) {}
}