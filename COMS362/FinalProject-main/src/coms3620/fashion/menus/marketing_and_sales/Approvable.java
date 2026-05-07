package coms3620.fashion.menus.marketing_and_sales;

import coms3620.fashion.menus.Option;
import coms3620.fashion.util.InputValidation;

public interface Approvable extends Option{
    
    public String getApprovalStatus();
    public void setApprovalStatus(String status);
    @Override
    public default void run() {
        System.out.println("Do you want to approve or decline this advertising relationship?");
        int response = InputValidation.OptionsInput(new String[]{"Approve", "Decline"});
        if(response == 0) {
            setApprovalStatus("approved");
        }
        if(response == 1) {
            setApprovalStatus("denied");
        }
    }
}
