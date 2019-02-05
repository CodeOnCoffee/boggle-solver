package org.arrr.boggle;

import java.util.Set;

public class Results {
    private Set<String> entries;

    public Results(Set<String> entries) {
        this.entries = entries;
    }

    public Set<String> getEntries() {
        return entries;
    }

    public void setEntries(Set<String> entries) {
        this.entries = entries;
    }
}
