package coms3620.fashion.departments.human_resources.repository;

import coms3620.fashion.departments.human_resources.Employee;
import coms3620.fashion.departments.human_resources.Review;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewRepo {

    private static final String FILE_NAME = "data/human_resources/review.csv";
    private List<Review> review = new ArrayList<>();


    public void loadReviews()
    {
        review.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line = br.readLine(); // skip header
            if (line == null) return;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                int reviewId = Integer.parseInt(data[0]);
                int revieweeID = Integer.parseInt(data[1]);
                int reviewerID = Integer.parseInt(data[2]);
                String comment = data[3].trim();
                String date = data[4].trim();


                review.add(new Review(reviewId, revieweeID, reviewerID, comment, date));
            }

            System.out.println("Reviews loaded from " + FILE_NAME);

        } catch (IOException e) {
            System.out.println("No existing CSV found.");
        }
    }

    public void saveReviews() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            pw.println("reviewId,revieweeId,reviewerId,comment,date");

            for (Review r : review) {
                pw.println(String.join(",",
                        String.valueOf(r.getReviewId()),
                        String.valueOf(r.getRevieweeId()),
                        String.valueOf(r.getReviewerID()),
                        r.getComment(),
                        r.getDate()
                ));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Review> getAllReviews() {
        return review;
    }

    public Review getReview(int reviewId) {

        for (Review r : review) {
            if (reviewId == r.getReviewId()) {
                return r;
            }
        }

        return null;
    }

}
