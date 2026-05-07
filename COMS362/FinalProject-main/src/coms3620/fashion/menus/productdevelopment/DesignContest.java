package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.departments.product_development.Prototype;
import coms3620.fashion.departments.product_development.PrototypeRepository;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.InputValidation;
import coms3620.fashion.util.Stdin;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class DesignContest implements Option {

    private final PrototypeRepository repo;

    public DesignContest(PrototypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public String getName() {
        return "Design Contest (Enter/Vote/Results)";
    }

    @Override
    public void run() {
        boolean contestOpen = isContestOpen();
        List<Prototype> entries = repo.findAll(); // remove .stream() filter

        System.out.println("====  INTERNAL DESIGN CONTEST  ====");
        System.out.println("Status: " + (contestOpen ? "OPEN" : "CLOSED"));
        if (!entries.isEmpty()) {
            Prototype leader = entries.stream()
                    .max(Comparator.comparingInt(this::voteCount)
                            .thenComparing(Comparator.comparing(Prototype::getId)))
                    .orElse(null);
            System.out.println("Current leader: " + leader.getConceptName()
                    + " (votes: " + voteCount(leader) + ")");
        }

        System.out.println("1. Enter contest");
        System.out.println("2. Vote");
        System.out.println("3. Close contest / Results");
        System.out.println("0. Back");
        int choice = InputValidation.IntegerRangeInput(0, 3);

        switch (choice) {
            case 1 ->
                enterContest(entries, contestOpen);
            case 2 ->
                vote(entries);
            case 3 ->
                closeContest(entries);
        }
    }

    private void enterContest(List<Prototype> entries, boolean open) {
        if (!open) {
            System.out.println("Contest is closed – no new entries.");
            return;
        }
        String user = askName(); // simple name attestation
        boolean alreadyEntered = entries.stream().anyMatch(p -> p.getLastNote().contains(user));
        if (alreadyEntered) {
            System.out.println("You already entered – leader board shown above.");
            return;
        }

        System.out.print("Concept name (≤30 chars): ");
        String concept = Stdin.nextLine().trim();
        if (concept.length() > 30) {
            concept = concept.substring(0, 30);
        }

        System.out.print("One-line pitch (≤80 chars): ");
        String pitch = Stdin.nextLine().trim();
        if (pitch.length() > 80) {
            pitch = pitch.substring(0, 80);
        }

        System.out.print("Preferred material: ");
        String material = Stdin.nextLine().trim();

        Prototype entry = new Prototype(concept, material);
        entry.setLastActor(user);
        entry.setLastNote("Contest entry – " + pitch);
        repo.add(entry); // saves CSV
        System.out.println("Entry submitted!  ID: " + entry.getId());
    }

    private void vote(List<Prototype> entries) {
        if (entries.isEmpty()) {
            System.out.println("No entries to vote on.");
            return;
        }
        String user = askName();
        if (hasVoted(user)) {
            System.out.println("You already voted – current standings:");
            printLeaderBoard(entries);
            return;
        }

        System.out.println("Vote for your favourite:");
        for (int i = 0; i < entries.size(); i++) {
            Prototype p = entries.get(i);
            System.out.printf("%d. %s  (%s)%n", i + 1, p.getConceptName(), p.getMaterials());
        }
        int idx = InputValidation.IntegerRangeInput(1, entries.size()) - 1;
        Prototype chosen = entries.get(idx);

        logVote(user, chosen.getId());
        System.out.println("Vote recorded for " + chosen.getConceptName());
    }

    private void closeContest(List<Prototype> entries) {
        if (entries.isEmpty()) {
            System.out.println("No entries – nothing to close.");
            return;
        }
        Prototype winner = entries.stream()
                .max(Comparator.comparingInt(this::voteCount)
                        .thenComparing(Comparator.comparing(Prototype::getId)))
                .orElse(null);
        int winningVotes = voteCount(winner);

        // move winner to approved & annotate
        winner.approve();
        winner.setLastNote("Contest Winner – " + winningVotes + " votes");
        repo.save();

        printTrophy(winner.getConceptName(), winningVotes);
        System.out.println("Contest closed.  Winner: " + winner.getConceptName());
    }

    /* ---------- tiny helpers ---------- */
    private boolean isContestOpen() {
        // simple flag file – create/open at will
        return !Files.exists(Paths.get("data/product_development/contest_closed.flag"));
    }

    private String askName() {
        System.out.print("Your name (for vote/entry log): ");
        return Stdin.nextLine().trim();
    }

    private boolean hasVoted(String user) {
        Path voteLog = Paths.get("data/product_development/contest_votes.csv");
        if (!Files.exists(voteLog)) {
            return false;
        }
        try (Stream<String> lines = Files.lines(voteLog)) {
            return lines.anyMatch(l -> l.split(",")[0].equalsIgnoreCase(user));
        } catch (IOException e) {
            return false;
        }
    }

    private void logVote(String user, UUID prototypeId) {
        Path log = Paths.get("data/product_development/contest_votes.csv");
        try {
            Files.createDirectories(log.getParent());
            Files.writeString(log, user + "," + prototypeId + "," + LocalDateTime.now() + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Vote log failed: " + e.getMessage());
        }
    }

    private int voteCount(Prototype p) {
        Path log = Paths.get("data/product_development/contest_votes.csv");
        if (!Files.exists(log)) {
            return 0;
        }
        try (Stream<String> lines = Files.lines(log)) {
            return (int) lines.filter(l -> UUID.fromString(l.split(",")[1]).equals(p.getId())).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private void printLeaderBoard(List<Prototype> entries) {
        entries.stream()
                .sorted(Comparator.comparingInt((Prototype p) -> voteCount(p)).reversed()
                        .thenComparing(Prototype::getId))
                .forEach(p -> System.out.printf("  %s – %d votes%n", p.getConceptName(), voteCount(p)));
    }

    private void printTrophy(String concept, int votes) {
        System.out.println("WINNER:" + concept + " with " + votes + " votes!");
    }
}
