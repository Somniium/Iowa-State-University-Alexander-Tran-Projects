package coms3620.fashion.departments.logistics;


public interface Trackable {
    public String getID();
    public String getStatus();
    public void updateStatus(Status status);
}
