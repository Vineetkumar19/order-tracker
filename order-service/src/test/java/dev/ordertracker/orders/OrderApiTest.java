package dev.ordertracker.orders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.when;
import java.util.UUID;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean InventoryClient inventory;

    @Test
    void createsAnOrderAndReadsItFromTheLocationHeader() throws Exception {
        when(inventory.available("NOTEBOOK")).thenReturn(5);
        var response = mvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" 
                        {"sku":"NOTEBOOK","quantity":2}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse();

        mvc.perform(get(response.getHeader("Location")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("NOTEBOOK"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void rejectsAnInvalidQuantity() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku":"NOTEBOOK","quantity":0}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForAnUnknownOrder() throws Exception {
        mvc.perform(get("/orders/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
