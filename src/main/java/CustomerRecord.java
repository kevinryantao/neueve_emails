import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.List;

public class CustomerRecord {

    private static DecimalFormat HUNDREDTHS = new DecimalFormat("#.00");

    String name;
    String email;
    int numPurchases;
    DateTime firstPurchase = null;
    DateTime lastPurchase = null;
    String address;
    double lifetimeValue;
    List<String> orderHistory;
    int numSubscriptions;
    int numBVPurchases;
    String firstName;
    String lastName;

    public CustomerRecord(String name, String email, String address){
        this.name = name;
        this.email = email;
        this.address = address;
        String[] addressParts = this.address.split(",");
        firstName = addressParts[0].toLowerCase().replaceAll("[^a-zA-Z ]", " ").trim();
        lastName = addressParts[1].trim();
        if(lastName.matches(".*\\d+.*") || lastName.toLowerCase().contains("box ")){
            String[] firstNameSplit = firstName.split(" ");
            if(firstNameSplit.length > 1){
                lastName = firstNameSplit[firstNameSplit.length -1];
                firstName = firstName.substring(0, firstName.length() - lastName.length()).trim();
            }
        }
        String[] firstNameParts = firstName.split(" ");
        firstName = "";
        for(int i = 0; i < firstNameParts.length; i++){
            if(firstNameParts[i].length() > 1){
                firstNameParts[i] = firstNameParts[i].substring(0,1).toUpperCase() + firstNameParts[i].substring(1);
            } else {
                firstNameParts[i] = firstNameParts[i].toUpperCase();
            }
        }
        firstName = StringUtils.join(firstNameParts, " ");

        numPurchases = 0;
        lifetimeValue = 0;
    }

    public void addPurchase(double gross, String type, DateTime purchaseDate, String itemTitle){
        if(type.equals("Subscription Payment")){
            numSubscriptions++;
            lifetimeValue += gross;
        } else if(type.equals("Website Payment") || type.equals("eBay Auction Payment")){
            numPurchases++;
            lifetimeValue += gross;
        } else if(type.equals("Shopping Cart Item")){
            if(itemTitle.contains("bv") || itemTitle.contains("BV")){
                numBVPurchases++;
            }
        }

        if(firstPurchase == null || firstPurchase.isAfter(purchaseDate)){
            firstPurchase = purchaseDate;
        }

        if(lastPurchase == null || lastPurchase.isBefore(purchaseDate)){
            lastPurchase = purchaseDate;
        }
    }

    public String toString(){

        String[] array = new String[] { email, "\"" + name + "\"", "\"" + firstName + "\"", "\"" + lastName + "\"", "\"" + address + "\"", HUNDREDTHS.format(lifetimeValue), Integer.toString(numPurchases), Integer.toString(numSubscriptions), Integer.toString(numBVPurchases), firstPurchase.toLocalDate().toString(), lastPurchase.toLocalDate().toString() };

        return StringUtils.join(array, ",");

    }

}
