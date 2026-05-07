package coms3620.fashion.departments.marketing_and_sales.adverts;

import java.io.File;

import coms3620.fashion.menus.marketing_and_sales.Approvable;
import coms3620.fashion.util.InputValidation;
import coms3620.fashion.util.Stdin;

public class AdvertisingRelationship implements Approvable{

    public AdvertisingRelationship() {
        System.out.println("What is the name of the company?");
        name = Stdin.nextLine();
    };

    public AdvertisingRelationship(Object[] object) {
        name = (String)object[0];
        approvalStatus = (String)object[1];
        contractFile = (String)object[2];
    };

    private String name;
    private String contractFile = "";
    private String approvalStatus = "pending";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run() {
        System.out.println("Do you want to approve or decline this advertising relationship?");
        int response = InputValidation.OptionsInput(new String[]{"Approve", "Decline"});
        if(response == 0) {
            setApprovalStatus("waiting for legal");
        }
        if(response == 1) {
            setApprovalStatus("denied");
        }
    }

    public void draftLegal() {
        System.out.println("Is there a contract for this advertising relationship?");
        int response = InputValidation.OptionsInput(new String[]{"Yes", "No"});
        if(response == 0) {
            setApprovalStatus("approved");
            System.out.println("Enter the contract file.");
            contractFile = Stdin.nextLine();
            try {
                File file = new File(contractFile);
            } catch (Exception e) {
                System.out.println("Contract file not found");
                setApprovalStatus("Denied");
            }
        }
        if(response == 1) {
            System.out.println("A contract file is required.");
            setApprovalStatus("waiting for legal");
        }
    }

    @Override
    public String getApprovalStatus() {
        return approvalStatus;
    }

    @Override
    public void setApprovalStatus(String status) {
        approvalStatus = status;
    }

    public String getContractFile() {
        return contractFile;
    }

    public Object[] getRowData() { 
        return new Object[]{"sss", name, approvalStatus, contractFile};
     };
    
}
