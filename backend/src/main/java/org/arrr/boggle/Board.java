package org.arrr.boggle;

import com.google.common.base.Preconditions;
import com.sun.javafx.binding.StringFormatter;

import javax.validation.constraints.NotNull;

public class Board {
    @NotNull
    private Integer boardSize = 4;

    @NotNull
    private String[][] values;

    public Board() {
    }

    public Board(int boardSize, String[][] values) {
        this.boardSize = boardSize;
        this.values = values;

        performChecks();
    }

    public Integer getBoardSize() {
        return boardSize;
    }

    public String[][] getValues() {
        return values;
    }

    public void setBoardSize(Integer boardSize) {
        this.boardSize = boardSize;
    }

    public void setValues(String[][] values) {
        this.values = values;
    }

    private void performChecks() {
        // Check that values is a square dataset matching the expected board size

        // Height of board
        Preconditions.checkArgument(this.values.length == boardSize,
            StringFormatter.format("Dimension of board data %d does not match board size %d", this.values.length, boardSize));

        // Width of each row
        for (int rowIdx = 0; rowIdx < values.length; rowIdx++) {
            String[] row = values[rowIdx];
            Preconditions.checkArgument(row.length == boardSize,
                StringFormatter.format("Dimension of board data row num %d does not match board size %d. Size: %d", rowIdx, boardSize, row.length));
        }
    }
}
