package com.cw2client;

import com.grpc.generated.*;
import com.grpc.generated.ConcertServiceGrpc;
import com.grpc.generated.MakeReservationServiceGrpc;
import com.grpc.generated.TicketServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class ConcertClient {
    private final ManagedChannel channel;
    private final TicketServiceGrpc.TicketServiceBlockingStub ticketStub;
    private final ConcertServiceGrpc.ConcertServiceBlockingStub concertStub;
    private final MakeReservationServiceGrpc.MakeReservationServiceBlockingStub reservationStub;

    public ConcertClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        ticketStub = TicketServiceGrpc.newBlockingStub(channel);
        concertStub = ConcertServiceGrpc.newBlockingStub(channel);
        reservationStub = MakeReservationServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    public void runCLI() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Concert Ticket Reservation Client ---");
            System.out.println("1. Add Concert");
            System.out.println("2. Delete Concert");
            System.out.println("3. List Concerts");
            System.out.println("4. Add Ticket");
            System.out.println("5. Update Ticket");
            System.out.println("6. Delete Ticket");
            System.out.println("7. List Tickets");
            System.out.println("8. Make Reservation");
            System.out.println("0. Exit");
            System.out.print("Select an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addConcert(scanner);
                    break;
                case 2:
                    deleteConcert(scanner);
                    break;
                case 3:
                    listConcerts(scanner);
                    break;
                case 4:
                    addTicket(scanner);
                    break;
                case 5:
                    updateTicket(scanner);
                    break;
                case 6:
                    deleteTicket(scanner);
                    break;
                case 7:
                    listTickets(scanner);
                    break;
                case 8:
                    makeReservation(scanner);
                    break;
                case 0:
                    shutdown();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void addConcert(Scanner scanner) {
        System.out.print("Concert ID: ");
        String id = scanner.nextLine();
        System.out.print("Concert Name: ");
        String name = scanner.nextLine();
        System.out.print("Date: ");
        String date = scanner.nextLine();

        Concert concert = Concert.newBuilder()
                .setId(id)
                .setName(name)
                .setDate(date)
                .setIsSentByPrimary(false)
                .build();

        OperationStatus result = concertStub.addConcert(concert);
        System.out.println("Status: " + result.getMessage());
    }

    private void deleteConcert(Scanner scanner) {
        System.out.print("Concert ID to delete: ");
        String id = scanner.nextLine();

        ConcertRequest request = ConcertRequest.newBuilder()
                .setConcertId(id)
                .setIsSentByPrimary(false)
                .build();

        OperationStatus result = concertStub.deleteConcert(request);
        System.out.println("Status: " + result.getMessage());
    }
    private void listConcerts(Scanner scanner) {
        System.out.print("Keyword (leave blank for all): ");
        String keyword = scanner.nextLine();

        Query query = Query.newBuilder()
                .setSearchKeyword(keyword)
                .setIsSentByPrimary(false)
                .build();

        ConcertList concerts = concertStub.listConcerts(query);

        System.out.println("--- Concerts ---");
        if (concerts.getConcertsList().isEmpty()) {
            System.out.println("No concerts found.");
        } else {
            for (Concert concert : concerts.getConcertsList()) {
                System.out.printf("ID: %s | Name: %s | Date: %s | Tickets: %d\n",
                        concert.getId(), concert.getName(), concert.getDate(), concert.getTicketsCount());
            }
        }
    }
    private void addTicket(Scanner scanner) {
        Ticket ticket = buildTicketFromInput(scanner);
        if (ticket == null) {
            return;
        }
        OperationStatus result = ticketStub.addTicket(ticket);
        System.out.println("Status: " + result.getMessage());
    }

    private void deleteTicket(Scanner scanner) {
        System.out.print("Ticket ID to delete: ");
        String id = scanner.nextLine();

        TicketRequest request = TicketRequest.newBuilder()
                .setTicketId(id)
                .setIsSentByPrimary(false)
                .build();

        OperationStatus result = ticketStub.deleteTicket(request);
        System.out.println("Status: " + result.getMessage());
    }

    private void listTickets(Scanner scanner) {
        System.out.print("Keyword (leave blank for all): ");
        String keyword = scanner.nextLine();

        Query query = Query.newBuilder()
                .setSearchKeyword(keyword)
                .setIsSentByPrimary(false)
                .build();

        TicketList tickets = ticketStub.listTickets(query);
        System.out.println("--- Tickets ---");
        for (Ticket t : tickets.getTicketsList()) {
            System.out.printf("ID: %s | Concert ID: %s | Type: %s | Price: %.2f | Qty: %d | AfterPartyQty: %d\n",
                    t.getId(), t.getConcertId(), t.getType(), t.getPrice(), t.getQuantity(), t.getAfterPartyQuantity());
        }
    }


    private void makeReservation(Scanner scanner) {
        System.out.print("Ticket ID: ");
        String ticketId = scanner.nextLine();
        System.out.print("Customer ID: ");
        String customerId = scanner.nextLine();
        System.out.print("Reservation Date: ");
        String date = scanner.nextLine();
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine());
        System.out.print("Include After-Party? (true/false): ");
        boolean afterParty = Boolean.parseBoolean(scanner.nextLine());

        ReservationRequest request = ReservationRequest.newBuilder()
                .setTicketId(ticketId)
                .setCustomerId(customerId)
                .setReservationDate(date)
                .setQuantity(qty)
                .setIncludesAfterParty(afterParty)
                .setIsSentByPrimary(false)
                .build();

        OperationStatus result = reservationStub.makeReservation(request);
        System.out.println("Status: " + result.getMessage());
    }

    private void updateTicket(Scanner scanner) {
        System.out.print("Ticket ID: ");
        String ticketId = scanner.nextLine();

        // Check if ticket exists
        TicketRequest request = TicketRequest.newBuilder()
                .setTicketId(ticketId)
                .setIsSentByPrimary(false)
                .build();
        OperationStatus existenceCheck = ticketStub.checkTicketExists(request);

        if (!existenceCheck.getSuccess()) {
            System.out.println("Ticket not found.");
            return;
        }

        System.out.print("Add concert ticket qty: ");
        int addQty = Integer.parseInt(scanner.nextLine());
        System.out.print("Add after-party ticket qty: ");
        int addAPQty = Integer.parseInt(scanner.nextLine());

        Query query = Query.newBuilder()
                .setSearchKeyword(ticketId)
                .build();

        TicketList result = ticketStub.listTickets(query);

        if (result.getTicketsCount() == 0) {
            System.out.println("Ticket not found.");
            return;
        }

        Ticket current = result.getTickets(0);
        Ticket updated = current.toBuilder()
                .setQuantity(addQty)
                .setAfterPartyQuantity(addAPQty)
                .setIsSentByPrimary(false)
                .build();

        OperationStatus response = ticketStub.updateTicket(updated);
        System.out.println("Status: " + response.getMessage());
    }


    private Ticket buildTicketFromInput(Scanner scanner) {
        int apQty = 0;

        System.out.print("Concert ID: ");
        String concertId = scanner.nextLine();

        // Check if concert exists
        ConcertRequest concertRequest = ConcertRequest.newBuilder()
                .setConcertId(concertId)
                .setIsSentByPrimary(false)
                .build();
        OperationStatus concertCheck = concertStub.checkConcertExists(concertRequest);

        if (!concertCheck.getSuccess()) {
            System.out.println("Concert not found.");
            return null;
        }

        System.out.print("Ticket ID (unique per ticket): ");
        String ticketId = scanner.nextLine();

        System.out.print("Type (e.g., VIP/Regular): ");
        String type = scanner.nextLine();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine());
        System.out.print("Includes After-Party? (true/false): ");
        boolean afterParty = Boolean.parseBoolean(scanner.nextLine());

        if (afterParty) {
            System.out.print("After-Party Quantity: ");
            apQty = Integer.parseInt(scanner.nextLine());
        } else {
            System.out.println("After-Party Quantity: 0");
        }

        return Ticket.newBuilder()
                .setId(ticketId)
                .setConcertId(concertId) // ✅ new field
                .setType(type)
                .setPrice(price)
                .setQuantity(qty)
                .setIncludesAfterParty(afterParty)
                .setAfterPartyQuantity(apQty)
                .setIsSentByPrimary(false)
                .build();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("INVALID INPUT: Use ConcertClient <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ConcertClient client = new ConcertClient(host, port);
        client.runCLI();
    }
}
