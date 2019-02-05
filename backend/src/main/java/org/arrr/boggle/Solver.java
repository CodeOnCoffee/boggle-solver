package org.arrr.boggle;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Solver {

    private Set<String> dictionary;
    private Board board;

    // Multi-thread safe
    private Map<String, Optional<List<WordSegment>>> cache = new ConcurrentHashMap<>();
    private Map<String, List<LetterCoordinate>> boardCache = new HashMap<>();

    public Solver(Set<String> dictionary, Board board) {
        this.dictionary = dictionary;
        this.board = board;
        populateBoardCache();
    }

    /**
     * Generate a map of String -> List[LetterCoordinate] for quick searches.
     */
    private void populateBoardCache() {
        String[][] values = board.getValues();
        for (int y = 0; y < values.length; y++) {
            String[] row = values[y];
            for (int x = 0; x < row.length; x++) {
                String letter = row[x].toLowerCase();
                boardCache.putIfAbsent(letter, new ArrayList<>());
                boardCache.get(letter).add(new LetterCoordinate(x, y, letter));
            }
        }
    }

    /**
     * Iterate over the dictionary of possible words to find matches on the board.
     *
     * <p>Notes: Lookups are parallelized across the default Fork/Join Pool
     *
     * @return Results contining words on the board
     */
    public Results solve() {
        // Brute force the dictionary. Multi-threaded using default Fork-Join pool
        Set<String> matches = dictionary.parallelStream().filter((word) -> {

            int cursor = 0;
            List<WordSegment> currentSegmentPaths = Collections.emptyList();

            // See if any portion of this word has been found already. Walks backward from whole word
            for (int len = word.length(); len > 0; len--) {
                String fragment = word.substring(0, len);
                if (cache.containsKey(fragment)) {
                    // Cache hit
                    Optional<List<WordSegment>> coordinates = cache.get(fragment);
                    if (coordinates.isPresent()) {
                        // We've previously computed all paths to this part of the word. Use cache
                        currentSegmentPaths = coordinates.get();
                        cursor = len;
                    } else {
                        // previously marked as not present. short-circuit solve for this word
                        return false;
                    }
                    break;
                }
            }

            // move forward searching the board for the next letter and see if it joins to the existing word paths.
            do {
                if (cursor == word.length()) {
                    // whole word has been found
                    return true;
                }

                String nextCharInWord = word.substring(cursor, cursor + 1);
                String wordFragment = word.substring(0, cursor + 1);

                // TODO: Handle QU

                // Find coordinates of next letter in board
                Optional<List<LetterCoordinate>> maybeNextCharPositions = locateInBoard("" + nextCharInWord);

                if (!maybeNextCharPositions.isPresent()) {
                    // next char not on board. mark cache
                    cache.put(wordFragment, Optional.empty());
                    return false;
                }

                List<LetterCoordinate> nextCharPositions = maybeNextCharPositions.get();

                if (cursor == 0) {
                    // first letter. Nothing to connect to
                    List<WordSegment> startingSegments = createStartingSegments(wordFragment, nextCharPositions);
                    cache.put(wordFragment, Optional.of(startingSegments));
                    currentSegmentPaths = startingSegments;
                    continue;
                }

                // See if any of the next char positions are adjacent to the known paths up til now
                List<WordSegment> adjacent = currentSegmentPaths.stream().flatMap(wordSegment -> wordSegment.to(nextCharPositions, wordFragment)).collect(toList());

                if (adjacent.isEmpty()) {
                    // No points match existing paths up to now. Mark cache as not present
                    cache.put(wordFragment, Optional.empty());
                    return false;
                }

                // put paths this word fragment in the cache
                cache.put(wordFragment, Optional.of(adjacent));
                currentSegmentPaths = adjacent;

            } while (cursor++ <= word.length());

            // Not found
            return false;

        }).collect(Collectors.toSet());

        return new Results(matches);
    }

    private List<WordSegment> createStartingSegments(String wordFragment, List<LetterCoordinate> startingCharPositions) {
        return startingCharPositions.stream().map(coord -> new WordSegment(Collections.singletonList(coord), wordFragment)).collect(toList());
    }

    private Optional<List<LetterCoordinate>> locateInBoard(String c) {
        return Optional.ofNullable(boardCache.getOrDefault(c, null));
    }

    /**
     * Represents whole or part of a word with a list containing coordinates of the word path.
     */
    private class WordSegment extends ArrayList<LetterCoordinate> {
        String wordFragment;

        public WordSegment(List<LetterCoordinate> path, String wordFragment) {
            super(path);
            this.wordFragment = wordFragment;
        }

        /**
         * Construct a stream of new Word Segments 1-to-many based on connecting the next letter coordinates to this path.
         *
         * @param coords coordinates of next letter on board
         * @param nextWordFragment next word fragment
         * @return
         */
        public Stream<WordSegment> to(List<LetterCoordinate> coords, String nextWordFragment) {
            // See if new coord is adjacent to our last location but not overlapping with this path
            LetterCoordinate lastLetterPos = get(size() - 1);

            return coords.stream().map(nextCoordinate -> {
                if (size() == 0 || (coordsAreAdjacent(nextCoordinate, lastLetterPos) && noLoopBack(nextCoordinate))) {
                    WordSegment newPath = new WordSegment(this, nextWordFragment);
                    newPath.add(nextCoordinate);
                    return newPath;
                }
                return null;
            }).filter(Objects::nonNull);

        }

        /**
         * Check to see if the coordinate is not already used in this WordSegment.
         */
        private boolean noLoopBack(LetterCoordinate coord) {
            return !this.contains(coord);
        }

        /**
         * Check if two coordinates are adjacent on the board.
         */
        private boolean coordsAreAdjacent(LetterCoordinate coord, LetterCoordinate lastLetterPos) {
            return Math.abs(lastLetterPos.y - coord.y) <= 1 && Math.abs(lastLetterPos.x - coord.x) <= 1;
        }
    }

    private class LetterCoordinate {
        int x;
        int y;
        String letter;

        public LetterCoordinate(int x, int y, String letter) {
            this.x = x;
            this.y = y;
            this.letter = letter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LetterCoordinate that = (LetterCoordinate) o;
            return x == that.x &&
                    y == that.y &&
                    Objects.equals(letter, that.letter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, letter);
        }
    }
}
