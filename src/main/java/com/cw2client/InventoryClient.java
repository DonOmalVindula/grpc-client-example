package com.cw2client;

import com.grpc.generated.*;
import com.grpc.generated.AddItemServiceGrpc;
import com.grpc.generated.ListItemsServiceGrpc;
import com.grpc.generated.UpdateItemServiceGrpc;
import com.grpc.generated.DeleteItemServiceGrpc;
import com.grpc.generated.PlaceOrderServiceGrpc;
import com.grpc.generated.MakeReservationServiceGrpc;
import com.grpc.generated.UpdateStockServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class InventoryClient {
    private ManagedChannel channel = null;
    private AddItemServiceGrpc.AddItemServiceBlockingStub addItemClientStub = null;
    private ListItemsServiceGrpc.ListItemsServiceBlockingStub listItemClientStub = null;
    private UpdateItemServiceGrpc.UpdateItemServiceBlockingStub updateItemClientStub = null;
    private DeleteItemServiceGrpc.DeleteItemServiceBlockingStub deleteItemClientStub = null;
    private PlaceOrderServiceGrpc.PlaceOrderServiceBlockingStub placeOrderClientStub = null;
    private MakeReservationServiceGrpc.MakeReservationServiceBlockingStub makeReservationClientStub = null;
    private UpdateStockServiceGrpc.UpdateStockServiceBlockingStub updateStockClientStub = null;
    private String host;
    private int port;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: InventoryClient <host> <port>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1].trim());

        InventoryClient client = new InventoryClient(host, port);
        client.initializeConnection();
        try {
            client.processUserRequests();
        } finally {
            client.closeConnection();
        }
    }

    public InventoryClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void initializeConnection() {
        System.out.println("Initializing connection to server at " + host + ":" + port);
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        addItemClientStub = AddItemServiceGrpc.newBlockingStub(channel);
        listItemClientStub = ListItemsServiceGrpc.newBlockingStub(channel);
        updateItemClientStub = UpdateItemServiceGrpc.newBlockingStub(channel);
        deleteItemClientStub = DeleteItemServiceGrpc.newBlockingStub(channel);
        placeOrderClientStub = PlaceOrderServiceGrpc.newBlockingStub(channel);
        makeReservationClientStub = MakeReservationServiceGrpc.newBlockingStub(channel);
        updateStockClientStub = UpdateStockServiceGrpc.newBlockingStub(channel);
    }

    private void closeConnection() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    private void processUserRequests() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter the operation to perform: ");
                System.out.println("1. List All Items");
                System.out.println("2. Add Item");
                System.out.println("3. Update Item");
                System.out.println("4. Delete Item");
                System.out.println("5. List Items");
                System.out.println("6. Place Order for Item Creation");
                System.out.println("7. Make Reservation for Item");
                System.out.println("8. Update Stocks");
                System.out.println("0. Exit");
                int choice = Integer.parseInt(scanner.nextLine());  // changed to read the whole line and parse it
                switch (choice) {
                    case 1:
                        listAllItems();
                        break;
                    case 2:
                        addItem(scanner);
                        break;
                    case 3:
                        updateItem(scanner);
                        break;
                    case 4:
                        deleteItem(scanner);
                        break;
                    case 5:
                        listItems(scanner);
                        break;
                    case 6:
                        placeOrder(scanner);
                        break;
                    case 7:
                        makeReservation(scanner);
                        break;
                    case 8:
                        updateStock(scanner);
                        break;
                    case 0:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input. Please enter a number.");
        }
    }

    private void listAllItems() {
        Query query = Query.newBuilder().build();
        ItemList list = listItemClientStub.listItems(query);
        if (list.getItemsCount() > 0) {
            System.out.println("Listing All Items:");
            list.getItemsList().forEach(item -> System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Price: " + item.getPrice() + ", Quantity: " + item.getQuantity() + ", Available: " + item.getAvailable()));
        } else {
            System.out.println("No items found.");
        }
    }

    private void addItem(Scanner scanner) {
        System.out.println("Enter Item ID:");
        String id = scanner.nextLine();
        System.out.println("Enter Item Name:");
        String name = scanner.nextLine();
        System.out.println("Enter Price:");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.println("Enter Quantity:");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.println("Is Available (true/false):");
        boolean available = Boolean.parseBoolean(scanner.nextLine());

        Item item = Item.newBuilder()
                .setId(id)
                .setName(name)
                .setPrice(price)
                .setQuantity(quantity)
                .setAvailable(available)
                .build();
        OperationStatus status = addItemClientStub.addItem(item);
        System.out.println("Add Item Status: " + status.getMessage());
    }

    private void listItems(Scanner scanner) {
        System.out.println("Enter search keyword:");
        String keyword = scanner.nextLine();

        Query query = Query.newBuilder()
                .setSearchKeyword(keyword)
                .build();
        ItemList list = listItemClientStub.listItems(query);
        if (list.getItemsCount() > 0) {
            System.out.println("Listing Items:");
            list.getItemsList().forEach(item -> System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Price: " + item.getPrice() + ", Quantity: " + item.getQuantity() + ", Available: " + item.getAvailable()));
        } else {
            System.out.println("No items found.");
        }
    }

    private void updateItem(Scanner scanner) {
        System.out.println("Enter Item ID to update:");
        String id = scanner.nextLine();
        System.out.println("Enter new Item Name (leave blank to not change):");
        String name = scanner.nextLine();
        System.out.println("Enter new Price (enter -1 to not change):");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.println("Enter new Quantity (enter -1 to not change):");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.println("Is Available (true/false, leave blank to not change):");
        String availableInput = scanner.nextLine();
        Boolean available = availableInput.isEmpty() ? null : Boolean.parseBoolean(availableInput);

        // Fetch existing item to update only the fields provided
        Query query = Query.newBuilder()
                .setSearchKeyword(id)
                .build();
        ItemList itemList = listItemClientStub.listItems(query);
        if (itemList.getItemsCount() == 0) {
            System.out.println("Item not found.");
            return;
        }
        Item existingItem = itemList.getItems(0);
        Item.Builder itemBuilder = existingItem.toBuilder();
        if (!name.isEmpty()) itemBuilder.setName(name);
        if (price >= 0) itemBuilder.setPrice(price);
        if (quantity >= 0) itemBuilder.setQuantity(quantity);
        if (available != null) itemBuilder.setAvailable(available);

        Item updatedItem = itemBuilder.build();
        OperationStatus status = updateItemClientStub.updateItem(updatedItem);
        System.out.println("Update Item Status: " + status.getMessage());
    }

    private void deleteItem(Scanner scanner) {
        System.out.println("Enter the ID of the item to delete:");
        String id = scanner.nextLine();

        ItemRequest request = ItemRequest.newBuilder()
                .setId(id)
                .build();
        OperationStatus status = deleteItemClientStub.deleteItem(request);
        System.out.println("Delete Item Status: " + status.getMessage());
    }

    private void placeOrder(Scanner scanner) {
        System.out.println("Enter Item ID for the order:");
        String itemId = scanner.nextLine();
        System.out.println("Enter Quantity needed:");
        int quantity = Integer.parseInt(scanner.nextLine());

        OrderRequest request = OrderRequest.newBuilder()
                .setItemId(itemId)
                .setQuantity(quantity)
                .build();

        OperationStatus status = placeOrderClientStub.placeOrder(request);
        System.out.println("Place Order Status: " + status.getMessage());
    }

    private void makeReservation(Scanner scanner) {
        System.out.println("Enter Item ID for the reservation:");
        String itemId = scanner.nextLine();
        System.out.println("Enter Quantity to reserve:");
        int quantity = Integer.parseInt(scanner.nextLine());

        ReservationRequest request = ReservationRequest.newBuilder()
                .setItemId(itemId)
                .setQuantity(quantity)
                .build();

        OperationStatus status = makeReservationClientStub.makeReservation(request);
        System.out.println("Make Reservation Status: " + status.getMessage());
    }

    private void updateStock(Scanner scanner) {
        System.out.println("Enter Item ID for stock update:");
        String itemId = scanner.nextLine();
        System.out.println("Enter additional quantity to add:");
        int additionalQuantity = Integer.parseInt(scanner.nextLine());

        StockUpdateRequest request = StockUpdateRequest.newBuilder()
                .setItemId(itemId)
                .setAdditionalQuantity(additionalQuantity)
                .build();

        OperationStatus status = updateStockClientStub.updateStock(request);
        System.out.println("Update Stock Status: " + status.getMessage());
    }


}
