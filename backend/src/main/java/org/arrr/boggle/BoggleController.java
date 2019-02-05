package org.arrr.boggle;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoggleController {

    private final Set<String> dictionary;

    BoggleController(){
        try {
            this.dictionary = new HashSet<>(IOUtils.readLines(getClass().getResourceAsStream("/dictionary.txt"), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("Dictionary could not be read", e);
        }
    }

    @PostMapping("api/v1/boggle/solve")
    public Results solve(@RequestBody Board board) {
        return new Solver(dictionary, board).solve();

    }

}
