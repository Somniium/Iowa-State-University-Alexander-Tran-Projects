package coms3620.fashion.departments.human_resources.service;

import coms3620.fashion.departments.human_resources.Employee;
import coms3620.fashion.departments.human_resources.Review;
import coms3620.fashion.departments.human_resources.repository.EmployeeRepo;
import coms3620.fashion.departments.human_resources.repository.ReviewRepo;
import coms3620.fashion.departments.human_resources.service.ManageEmployees;
import java.time.LocalDate;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ManageReviews {

    private ReviewRepo reviewRepo = new ReviewRepo();
    private List<Review> reviews = new ArrayList<>();

    public void loadReviews() {
        reviewRepo.loadReviews();
    }

    public List<Review> getReviews() {
        return reviewRepo.getAllReviews();
    }

    public void saveReviews() {
        reviewRepo.saveReviews();
    }


    public List<Review> getReviewsByEmployeeId(int id) {
        List<Review> results = new ArrayList<>();

        for (Review r : reviewRepo.getAllReviews()) {
            if (r.getRevieweeId() == id) {
                results.add(r);
            }
        }
        return results;
    }

    public List<Review> getReviewsLeftByEmployeeId(int id) {
        List<Review> results = new ArrayList<>();
        for (Review r : reviewRepo.getAllReviews()) {
            if (r.getReviewerID() == id) {
                results.add(r);
            }
        }
        return results;
    }




    public void printReviewsByEmployeeId(int id, ManageEmployees sm) {
        List<Review> results = getReviewsByEmployeeId(id);

        if (results.isEmpty()) {
            System.out.println("No one has left a review for you yet.");
            return;
        }

        System.out.println("These employees have left reviews for you:\n");

        for (Review review : results) {

            // Get the employee who wrote the review
            Employee reviewer = sm.getEmployeeForReview(review.getReviewerID());
            String reviewerName = (reviewer != null) ? reviewer.getName() : "(Unknown Employee)";

            System.out.println("Review #" + review.getReviewId());
            System.out.println("  From : " + reviewerName + " (ID " + review.getReviewerID() + ")");
            System.out.println("  Date : " + review.getDate());
            System.out.println("  Text : " + review.getComment());
            System.out.println();
        }
    }


    public void printReviewsMadeByEmployeeID(int reviewerId, ManageEmployees sm) {
        List<Review> results = getReviewsLeftByEmployeeId(reviewerId);

        if (results.isEmpty()) {
            System.out.println("You have not left any reviews.");
            return;
        }

        System.out.println("You left these reviews:\n");

        for (Review review : results) {
            // Get the employee that this review is ABOUT
            Employee reviewee = sm.getEmployeeForReview(review.getRevieweeId());
            String revieweeName = (reviewee != null) ? reviewee.getName() : "(unknown employee)";

            System.out.println("Review #" + review.getReviewId());
            System.out.println("  Date : " + review.getDate());
            System.out.println("  For  : " + revieweeName + " (ID " + review.getRevieweeId() + ")");
            System.out.println("  Text : " + review.getComment());
            System.out.println();
        }
    }



    public void addReview(int revieweeId, int reviewerID, String comment) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        int reviewId = reviewRepo.getAllReviews().size() + 1;

        Review r = new Review(reviewId, revieweeId, reviewerID, comment, date);

        // Add to the repo's list, not a separate one
        reviewRepo.getAllReviews().add(r);

        // Tell the repo to write everything back to CSV
        reviewRepo.saveReviews();

        System.out.println("The review number is " + reviewId + " and it has been added.");
    }

    public boolean deleteReviewByIdForReviewer(int reviewerId, int reviewId) {
        List<Review> all = reviewRepo.getAllReviews();

        for (int i = 0; i < all.size(); i++) {
            Review r = all.get(i);

            // Only allow deleting reviews that THIS employee wrote
            if (r.getReviewId() == reviewId && r.getReviewerID() == reviewerId) {
                all.remove(i);
                reviewRepo.saveReviews();   // rewrite CSV after deletion
                return true;
            }
        }

        return false;  // not found or doesn't belong to this reviewer
    }









}
