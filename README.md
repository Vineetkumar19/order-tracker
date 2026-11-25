# GCP Tutorial | Order Tracker, Lab 1A

## Goal

Run two Java processes. Order owns order creation. Inventory owns the
availability lookup. Order asks Inventory over HTTP before saving an order.
This lab checks availability only; it does not reserve or decrement stock.
Orders are still stored in memory. PostgreSQL comes in the next lab.

## Run in VS Code on Windows

Extract the ZIP and open the extracted `order-tracker-lab-1` folder.
Stop the old Lab 0 app before starting Lab 1A, since both use port 8080.

**Terminal 1** (from the extracted root):

```powershell
cd inventory-service
.\mvnw.cmd spring-boot:run
```

**Terminal 2** (from the extracted root):

```powershell
cd order-service
.\mvnw.cmd spring-boot:run
```

**Terminal 3**:

```powershell
Invoke-RestMethod 'http://localhost:8081/inventory/NOTEBOOK'
$base = 'http://localhost:8080'
$body = @{ sku = 'NOTEBOOK'; quantity = 2 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/orders" -ContentType 'application/json' -Body $body | Format-List
```

If the Maven run goal fails in your environment, open each service's
`*ServiceApplication.java` and choose **Run Java** in VS Code after both
Maven projects are imported. Keep the two processes running independently.

## Break it on purpose

1. Request 2 NOTEBOOK units. Expected: HTTP 201, new ID.
2. Request 6 units. Expected: HTTP 409 because Inventory reports 5.
3. Stop **only Inventory** (Ctrl+C in Terminal 1). Request 2 again.
   Expected: HTTP 503, even though Order's own /actuator/health still returns UP.
4. Restart Inventory and repeat the request. Expected: HTTP 201 again.

Use this in Terminal 3 to choose a different quantity:

```powershell
$body = @{ sku = 'NOTEBOOK'; quantity = 6 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/orders" -ContentType 'application/json' -Body $body
```

## Read the three relevant files

- **InventoryController.java** owns the sample stock data. Unknown SKUs return 0.
- **InventoryClient.java** makes a bounded HTTP call: 1 second connect timeout,
  2 second read timeout. An unavailable dependency becomes 503; a malformed or
  failed upstream response becomes 502. The base URL is configurable with
  INVENTORY_BASE_URL, so Order does not have to be rebuilt for another host.
- **OrderService.java** asks for availability before writing. If stock is
  insufficient it returns 409. A network error is not the same as zero stock.

## Your change

Add `PENCIL` with stock 3 in InventoryController. Restart only Inventory, then
place an order for 3 pencils and one for 4. Predict each result before sending.
Order stays running and needs no code change: it reads Inventory's response.
