package com.bingo;

public class App {

    public static void main(String[] args) {
        TicketGenerator strip = new TicketGenerator();
        strip.generateTickets(Integer.parseInt("3"));
    }
}
