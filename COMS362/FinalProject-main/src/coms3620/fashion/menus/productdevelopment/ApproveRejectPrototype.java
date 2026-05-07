/**
 * @author Alexander Tran
 */
package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.departments.product_development.Prototype;
import coms3620.fashion.departments.product_development.PrototypeRepository;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.InputValidation;
import coms3620.fashion.util.Stdin;
import java.util.List;

public class ApproveRejectPrototype implements Option {

    private final PrototypeRepository repo;

    public ApproveRejectPrototype(PrototypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public String getName() {
        return "Approve/Reject Prototype (with notes)";
    }

    @Override
    public void run() {
        List<Prototype> all = repo.findAll();
        if (all.isEmpty()) {
            System.out.println("No prototypes available.");
            return;
        }

        // 1.  show list
        for (int i = 0; i < all.size(); i++) {
            Prototype p = all.get(i);
            String status = p.isApproved() ? "APPROVED" : "PENDING";
            System.out.printf("%d. %s [%s]%n", i + 1, p, status);
        }

        // 2.  pick prototype
        System.err.println("Select prototype to approve/reject:");
        int idx = InputValidation.IntegerRangeInput(1, all.size()) - 1;
        Prototype chosen = all.get(idx);

        // 3.  ATTESTATION – name is **mandatory**
        System.out.print("Enter your full name (required): ");
        String actor = Stdin.nextLine().trim();
        while (actor.isEmpty()) {
            System.out.print("Name cannot be blank – re-enter: ");
            actor = Stdin.nextLine().trim();
        }

        // 4.  optional note
        System.out.print("Add a note (optional, press ENTER to skip): ");
        String note = Stdin.nextLine().trim();

        // 5.  approve / reject
        System.out.println("1) Approve   2) Reject   0) Cancel");
        int choice = InputValidation.IntegerRangeInput(0, 2);
        if (choice == 0) {
            return;
        }

        if (choice == 1) {
            chosen.approve();
        } else {
            chosen.unapprove();
        }

        // 6.  audit trail
        chosen.setLastActor(actor);
        chosen.setLastNote(note);
        repo.save();

        System.out.println("Prototype " + chosen.getId() + " has been "
                + (choice == 1 ? "approved" : "rejected") + ".");
        System.out.printf("Attested by: %s%n", actor);
        if (!note.isEmpty()) {
            System.out.printf("Note: %s%n", note);
        }
    }
}
