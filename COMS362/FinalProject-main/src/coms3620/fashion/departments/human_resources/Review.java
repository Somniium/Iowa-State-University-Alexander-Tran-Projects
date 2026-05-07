package coms3620.fashion.departments.human_resources;

public class Review {

    int reviewId;
    int revieweeId;
    int reviewerID;
    String comment;
    String date;

    public Review(int reviewId, int revieweeId, int reviewerID, String comment, String date){

    this.reviewId = reviewId;
    this.revieweeId = revieweeId;
    this.reviewerID = reviewerID;
    this.comment = comment;
    this.date = date;

    }
    public int getReviewId() {   // capital R
        return reviewId;
    }
    public int getRevieweeId() {
        return revieweeId;
    }
    public int getReviewerID(){
        return reviewerID;
    }
    public String getComment(){
        return comment;
    }
    public String getDate(){
        return date;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public void setRevieweeId(int revieweeId) {
        this.revieweeId = revieweeId;
    }

    public void setReviewerID(int reviewerID) {
        this.reviewerID = reviewerID;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
