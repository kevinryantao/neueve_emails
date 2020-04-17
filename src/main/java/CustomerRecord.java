import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.List;

public class CustomerRecord {

    private static DecimalFormat HUNDREDTHS = new DecimalFormat("#.00");

    // DEPRECATED
    String name;
    int numPurchases;
    String address;
    double lifetimeValue;
    List<String> orderHistory;
    int numSubscriptions;
    int numBVPurchases;


    String email;
    String firstName;
    String lastName;

    String street;
    String zip;
    String city;
    String state;
    String country;

    int checkoutCount = 0;
    int cartItemCount = 0;
    boolean isSubscriber = false;
    DateTime subscribeStartDate = null;

    int silkCount;
    int silverCount;
    int goldCount;
    int bvCount;
    int creamCount;
    int applicatorCount;
    int assortedCount;

    String lastTransactionId;

    DateTime firstPurchase = null;
    DateTime lastPurchase = null;

    public CustomerRecord(String email,
                          String firstName,
                          String lastName,
                          String street,
                          String zip,
                          String city,
                          String state,
                          String country,
                          int checkoutCount,
                          int cartItemCount,
                          boolean isSubscriber,
                          DateTime subscribeStartDate,
                          int silkCount,
                          int silverCount,
                          int goldCount,
                          int bvCount,
                          int creamCount,
                          int applicatorCount,
                          int assortedCount,
                          String lastTransactionId,
                          DateTime firstPurchase,
                          DateTime lastPurchase) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.zip = zip;
        this.city = city;
        this.state = state;
        this.country = country;
        this.checkoutCount = checkoutCount;
        this.cartItemCount = cartItemCount;
        this.isSubscriber = isSubscriber;
        this.subscribeStartDate = subscribeStartDate;
        this.silkCount = silkCount;
        this.silverCount = silverCount;
        this.goldCount = goldCount;
        this.bvCount = bvCount;
        this.creamCount = creamCount;
        this.applicatorCount = applicatorCount;
        this.assortedCount = assortedCount;
        this.lastTransactionId = lastTransactionId;
        this.firstPurchase = firstPurchase;
        this.lastPurchase = lastPurchase;
    }

    // constructor for creating a CustomerRecord from PayPal
    public CustomerRecord(String name,
                          String email,
                          String address,
                          String street,
                          String city,
                          String state,
                          String zip,
                          String country) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;

        String[] addressParts = this.address.split(",");
        firstName = addressParts[0].toLowerCase().replaceAll("[^a-zA-Z ]", " ").trim();
        if (addressParts.length < 2) {
            System.out.println(addressParts[0]);
            System.out.println(name);
            System.out.println(email);
        }
        lastName = addressParts[1].trim();
        if (lastName.matches(".*\\d+.*") || lastName.toLowerCase().contains("box ")) {
            String[] firstNameSplit = firstName.split(" ");
            if (firstNameSplit.length > 1) {
                lastName = firstNameSplit[firstNameSplit.length - 1];
                firstName = firstName.substring(0, firstName.length() - lastName.length()).trim();
            }
        }
        String[] firstNameParts = firstName.split(" ");
        firstName = "";
        for (int i = 0; i < firstNameParts.length; i++) {
            if (firstNameParts[i].length() > 1) {
                firstNameParts[i] = firstNameParts[i].substring(0, 1).toUpperCase() + firstNameParts[i].substring(1);
            } else {
                firstNameParts[i] = firstNameParts[i].toUpperCase();
            }
        }
        firstName = StringUtils.join(firstNameParts, " ");

        numPurchases = 0;
        lifetimeValue = 0;

        checkoutCount = 0;
        cartItemCount = 0;
    }

    public void addPurchase(double gross, String type, DateTime purchaseDate, String itemTitle, String itemId, int quantity, String transactionId) {
        if (type.equals("Subscription Payment")) {
            isSubscriber = true;
            if(subscribeStartDate == null || subscribeStartDate.isAfter(purchaseDate)){
                subscribeStartDate = purchaseDate;
            }
            numSubscriptions++;
            lifetimeValue += gross;
            checkoutCount++;
            lastTransactionId = transactionId;
            addQuantity(itemId, quantity);
        } else if (type.equals("Website Payment") || type.equals("eBay Auction Payment")) {
            numPurchases++;
            lifetimeValue += gross;
            checkoutCount++;
            lastTransactionId = transactionId;
        } else if (type.equals("Shopping Cart Item")) {
            if (itemTitle.contains("ubscription") || itemTitle.contains("***") || itemTitle.contains("3 inserts")){
                return;
            }
            if (itemTitle.contains("bv") || itemTitle.contains("BV")) {
                numBVPurchases++;
            }
            addQuantity(itemId, quantity);
        }

        if (firstPurchase == null || firstPurchase.isAfter(purchaseDate)) {
            firstPurchase = purchaseDate;
        }

        if (lastPurchase == null || lastPurchase.isBefore(purchaseDate)) {
            lastPurchase = purchaseDate;
        }
    }

    private void addQuantity(String itemId, int quantity) {
        int realQuantity = Math.max(1, quantity);
        if(itemId.equals("silk") || itemId.equals("silk-ca")){
            this.silkCount += realQuantity;
        }
        if(itemId.equals("silver") || itemId.equals("silver-ca")){
            this.silverCount += realQuantity;
        }
        if(itemId.equals("gold") || itemId.equals("gold-ca")){
            this.goldCount += realQuantity;
        }
        if(itemId.equals("cream")){
            this.creamCount += realQuantity;
        }
        if(itemId.equals("assorted")){
            this.assortedCount += realQuantity;
        }
        if(itemId.equals("applicator")){
            this.applicatorCount += realQuantity;
        }
        if(itemId.equals("bv-clearing-kit")){
            this.bvCount += realQuantity;
        }
        if(itemId.equals("silk x 4")){
            this.silkCount += 4 * realQuantity;
        }
        if(itemId.equals("silver x 4")){
            this.silverCount += 4 * realQuantity;
        }
        if(itemId.equals("gold x 4")){
            this.goldCount += 4 * realQuantity;
        }
        if(itemId.equals("bv-clearing-kit x 4")){
            this.bvCount += 4 * realQuantity;
        }

        if(itemId.contains("x 4")){
            this.cartItemCount += 4 * realQuantity;
        } else {
            this.cartItemCount += realQuantity;
        }
    }

    // this customerRecord is the old one, otherCustomerRecord is the new one
    public void mergeWithOtherCustomerRecord(CustomerRecord otherCustomerRecord) {
        if(!this.email.equals(otherCustomerRecord.email)){
            return;
        }

        this.checkoutCount += otherCustomerRecord.checkoutCount;
        this.cartItemCount += otherCustomerRecord.cartItemCount;
        this.isSubscriber = this.isSubscriber || otherCustomerRecord.isSubscriber;
        if (otherCustomerRecord.isSubscriber){
            if(this.subscribeStartDate == null || this.subscribeStartDate.isAfter(otherCustomerRecord.subscribeStartDate)) {
                this.subscribeStartDate = otherCustomerRecord.subscribeStartDate;
            }
        }
        this.silkCount += otherCustomerRecord.silkCount;
        this.silverCount += otherCustomerRecord.silverCount;
        this.goldCount += otherCustomerRecord.goldCount;
        this.bvCount += otherCustomerRecord.bvCount;
        this.creamCount += otherCustomerRecord.creamCount;
        this.applicatorCount += otherCustomerRecord.applicatorCount;
        this.assortedCount += otherCustomerRecord.assortedCount;

        this.lastTransactionId = otherCustomerRecord.lastTransactionId;
        this.lastPurchase = otherCustomerRecord.lastPurchase;
    }

    public String toString() {

        int subscriber = isSubscriber ? 1 : 0;

        String subscribeStartString = "";

        if(subscribeStartDate != null){
            subscribeStartString = subscribeStartDate.toLocalDate().toString();
        }

        String[] array = new String[]{email,
                "\"" + firstName + "\"",
                "\"" + lastName + "\"",

                "\"" + street + "\"",
                "\"" + zip + "\"",
                "\"" + city + "\"",
                "\"" + state + "\"",
                "\"" + country + "\"",

                Integer.toString(checkoutCount),
                Integer.toString(cartItemCount),
                Integer.toString(subscriber),
                subscribeStartString,

                Integer.toString(silkCount),
                Integer.toString(silverCount),
                Integer.toString(goldCount),
                Integer.toString(bvCount),
                Integer.toString(creamCount),
                Integer.toString(applicatorCount),
                Integer.toString(assortedCount),
                lastTransactionId,

                firstPurchase.toLocalDate().toString(),
                lastPurchase.toLocalDate().toString()};

        return StringUtils.join(array, ",");

    }

}
