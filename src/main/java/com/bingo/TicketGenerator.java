package com.bingo;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TicketGenerator {

    public static final int BINGO_90_NUMBERS = 90;
    public static final int STRIP_TICKETS = 6;
    public static final int STRIP_ROWS = 3;
    public static final int TICKET_COLUMNS = 9;
    public static final int MAX_ROW_NUMBERS = 5;
    public static final int TICKET_ROWS = 18;

    private int[][] bingoTicket;

    public List<BingoTicket> generateTickets(Integer count) {

        List<BingoTicket> bingoStrips = new ArrayList<>();
        IntStream.range(0, count).boxed().forEach(strip -> bingoStrips.add(this.generateTicket()));
        return bingoStrips;
    }

    public BingoTicket generateTicket() {
        Random random = new Random();
        this.bingoTicket = new int[TICKET_ROWS][TICKET_COLUMNS];

        // counts numbers added per row
        int[] rowNumbersCounter = new int[TICKET_ROWS];

        //  list of 90 numbers
        List<Integer> fullNumberList = IntStream.rangeClosed(1, BINGO_90_NUMBERS)
                .boxed().collect(Collectors.toList());

        // initialize the list to store lists of numbers per column
        List<List<Integer>> columnNumberLists = new ArrayList<>();
        for (int i = 0; i < TICKET_COLUMNS; i++) {
            columnNumberLists.add(new ArrayList<>());
        }

        // distribute numbers per column
        fullNumberList.forEach(number -> {
            int columnId = Math.min(number/10, 8);
            columnNumberLists.get(columnId).add(number);
        });

        // shuffle column lists
        columnNumberLists.forEach(list -> Collections.shuffle(list, random));

        for (int colId = 0; colId < TICKET_COLUMNS; colId++) {
            for (int ticketId = 0; ticketId < STRIP_TICKETS; ticketId++) {
                int randomTicketRowIndex;
                int rowId;
                do {
                    randomTicketRowIndex = random.nextInt(STRIP_ROWS);
                    rowId = ticketId * STRIP_ROWS + randomTicketRowIndex;
                } while (rowNumbersCounter[rowId] >= MAX_ROW_NUMBERS);

                bingoTicket[rowId][colId] = columnNumberLists.get(colId).get(0);
                rowNumbersCounter[rowId]++;
                fullNumberList.remove(columnNumberLists.get(colId).get(0));
                int finalRowId = rowId;
                int finalColId = colId;
                columnNumberLists.get(colId).removeIf(number -> number == bingoTicket[finalRowId][finalColId]);
            }
        }

         fullNumberList.forEach(number -> {
            int columnId = Math.min(number/10, 8);
            IntStream.range(0, TICKET_ROWS).boxed()
                    .filter(rowId -> bingoTicket[rowId][columnId] == 0 && rowNumbersCounter[rowId] < MAX_ROW_NUMBERS)
                    .findFirst()
                    .ifPresentOrElse(
                            rowId -> {
                                bingoTicket[rowId][columnId] = number;
                                rowNumbersCounter[rowId]++;
                            }, () -> {
                                // find first row that has less than max items
                                int rowIdForNumberToBeAssigned = IntStream.range(0, rowNumbersCounter.length).boxed()
                                        .sorted(Collections.reverseOrder())
                                        .filter(rowId -> rowNumbersCounter[rowId] < MAX_ROW_NUMBERS)
                                        .findFirst().orElse(-1);
                                swapWithNumberFromDifferentRow(rowNumbersCounter, rowIdForNumberToBeAssigned, number, columnId);
                            });
        });
        this.sortTicketColumns();
        return new BingoTicket(bingoTicket);
    }


     //Swap the last remaining unassigned numbers
    private void swapWithNumberFromDifferentRow(int[] rowNumbersCounter, int rowIdForNumberToBeAssigned, int number, int columnId) {
        AtomicBoolean isNumberAssignedToRow = new AtomicBoolean(false);
        IntStream.rangeClosed(0, rowNumbersCounter.length - 1).boxed()
                .sorted(Collections.reverseOrder())
                .peek(rowId -> IntStream.range(0, TICKET_COLUMNS).boxed()
                        .filter(colId -> rowNumbersCounter[rowId] == MAX_ROW_NUMBERS
                                && bingoTicket[rowId][colId] != 0 && bingoTicket[rowId][columnId] == 0
                                && bingoTicket[rowIdForNumberToBeAssigned][colId] == 0)
                        .findFirst().ifPresent(colId -> {
                            bingoTicket[rowId][columnId] = number;
                            bingoTicket[rowIdForNumberToBeAssigned][colId] = bingoTicket[rowId][colId];
                            bingoTicket[rowId][colId] = 0;
                            rowNumbersCounter[rowIdForNumberToBeAssigned]++;
                            isNumberAssignedToRow.set(true);
                        }))
                .anyMatch(n -> isNumberAssignedToRow.get());
    }

    private void sortTicketColumns() {
        IntStream.range(0, STRIP_TICKETS).boxed()
                .forEach(stripId -> IntStream.range(0, TICKET_COLUMNS).boxed()
                        .forEach(rowId -> {
                            List<Integer> stripColumn = Arrays.asList(bingoTicket[STRIP_ROWS * stripId][rowId],
                                    bingoTicket[STRIP_ROWS * stripId + 1][rowId],
                                    bingoTicket[STRIP_ROWS * stripId + 2][rowId]);
                            stripColumn.sort((o1, o2) -> {
                                if (o1 == 0 || o2 == 0 || o1.equals(o2)) return 0;
                                if (o1 < o2) return -1;
                                return 1;
                            });
                            if (stripColumn.stream().filter(x->x==0).count() == 1 && stripColumn.get(1) == 0 &&
                                    stripColumn.get(0) > stripColumn.get(2)) {
                                Collections.swap(stripColumn, 0, 2);
                            }
                            bingoTicket[STRIP_ROWS * stripId][rowId] = stripColumn.get(0);
                            bingoTicket[STRIP_ROWS * stripId + 1][rowId] = stripColumn.get(1);
                            bingoTicket[STRIP_ROWS * stripId + 2][rowId] = stripColumn.get(2);
                        }));
    }
}
