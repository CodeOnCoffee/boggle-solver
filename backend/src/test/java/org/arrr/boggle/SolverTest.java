package org.arrr.boggle;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class SolverTest {

    @Test
    public void solve() {
        Set<String> dictionary = new HashSet<>(Arrays.asList("foo", "food", "tie", "bar", "rat", "bars"));

        String[][] board = new String[][] {
                new String[] {"f", "b", "a", "r"},
                new String[] {"o", "b", "a", "g"},
                new String[] {"f", "o", "d", "r"},
                new String[] {"f", "b", "a", "s"}
        };

        Solver solver = new Solver(dictionary, new Board(4, board));
        Results results = solver.solve();
        assertNotNull(results);

        // Due to parallel processing the order is not ensured
        assertThat(results.getEntries(), containsInAnyOrder("foo", "food", "bar", "bars"));
    }

    @Test
    public void solveIgnoresOverlaps() {
        Set<String> dictionary = new HashSet<>(Arrays.asList("foo", "goo", "googgle"));

        String[][] board = new String[][] {
                new String[] {"l", "e", "a", "r"},
                new String[] { /*overlap->*/ "g", "o", "a", "g"},
                new String[] { /*overlap->*/ "g", "o", "f", "r"},
                new String[] {"l", "e", "a", "s"}
        };

        Solver solver = new Solver(dictionary, new Board(4, board));
        Results results = solver.solve();
        assertNotNull(results);

        // Due to parallel processing the order is not ensured
        assertThat(results.getEntries(), containsInAnyOrder("foo", "goo"));
    }

}