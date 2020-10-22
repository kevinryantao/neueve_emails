import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.*;

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

    String street1;
    String street2 = "";
    String zip;
    String city;
    String state;
    String country;
    String phoneNumber = "";

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

    Map<String, Map<String, Integer>> txnIdToItemIdAndQuantity = new HashMap<String, Map<String, Integer>>();

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
        this.street1 = street;
        this.street2 = "";
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
                          String street2,
                          String city,
                          String state,
                          String zip,
                          String country,
                          String phoneNumber) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.street1 = street;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.phoneNumber = phoneNumber;

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

    // todo: need to check if itemTitle and itemId are blank
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
            itemId = starsEdited(itemTitle, itemId);
            addQuantity(itemId, quantity, transactionId);
        } else if (type.equals("Website Payment") || type.equals("eBay Auction Payment")) {
            numPurchases++;
            lifetimeValue += gross;
            checkoutCount++;
            lastTransactionId = transactionId;
        } else if (type.equals("Shopping Cart Item")) { // prevents double counting of subscriptions
            if (itemTitle.contains("ubscription") || itemTitle.contains("***") || itemTitle.contains("3 inserts")){
                return;
            }
            if (itemTitle.contains("bv") || itemTitle.contains("BV")) {
                numBVPurchases++;
            }
            addQuantity(itemId, quantity, transactionId);
        }

        if (firstPurchase == null || firstPurchase.isAfter(purchaseDate)) {
            firstPurchase = purchaseDate;
        }

        if (lastPurchase == null || lastPurchase.isBefore(purchaseDate)) {
            lastPurchase = purchaseDate;
        }
    }

    // 10/21/2020 It's possible for the "itemId" to be blank sometimes. We need to catch this case
    private String starsEdited(String itemTitle, String itemId) {
        if(!itemTitle.contains("*") && !itemId.isEmpty()){
            return itemId;
        }
        if(itemTitle.contains("clear") || itemTitle.contains("clear")){
            return "bv-clearing-kit";
        }
        if(itemTitle.contains("silk") || itemTitle.contains("Silk")){
            return "silk";
        }
        if(itemTitle.contains("silver") || itemTitle.contains("Silver")){
            return "silver";
        }
        if(itemTitle.contains("gold") || itemTitle.contains("Gold")){
            return "gold";
        }
        if(itemTitle.contains("cream") || itemTitle.contains("Cream")){
            return "cream";
        }
        return itemId;
    }

    private void addQuantity(String itemId, int quantity, String transactionId) {
        int realQuantity = Math.max(1, quantity);
        String realItemId = itemId;

        Map<String, Integer> shoppingCart = txnIdToItemIdAndQuantity.get(transactionId);
        if(shoppingCart == null){
            shoppingCart = new HashMap<String, Integer>();
            txnIdToItemIdAndQuantity.put(transactionId,shoppingCart);
        }

        if(itemId.equals("silk") || itemId.equals("silk-ca") || itemId.equals("1")){
            this.silkCount += realQuantity;
            realItemId = "silk";
        }
        if(itemId.equals("silver") || itemId.equals("silver-ca") || itemId.equals("2")){
            this.silverCount += realQuantity;
            realItemId = "silver";
        }
        if(itemId.equals("gold") || itemId.equals("gold-ca") || itemId.equals("3")){
            this.goldCount += realQuantity;
            realItemId = "gold";
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
            realItemId = "silk";
        }
        if(itemId.equals("silver x 4")){
            this.silverCount += 4 * realQuantity;
            realItemId = "silver";
        }
        if(itemId.equals("gold x 4")){
            this.goldCount += 4 * realQuantity;
            realItemId = "gold";
        }
        if(itemId.equals("bv-clearing-kit x 4")){
            this.bvCount += 4 * realQuantity;
            realItemId = "bv-clearing-kit";
        }
        if(itemId.equals("cream x 4")){
            this.creamCount += 4 * realQuantity;
            realItemId = "cream";
        }

        if(itemId.contains("x 4")){
            this.cartItemCount += 4 * realQuantity;
            shoppingCart.put(realItemId, 4 * realQuantity);
        } else {
            this.cartItemCount += realQuantity;
            shoppingCart.put(realItemId, realQuantity);
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
                "\"" + street1 + street2 + "\"",
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
                lastPurchase.toLocalDate().toString(),
                "\"" + phoneNumber + "\""
        };

        return StringUtils.join(array, ",");

    }

    public boolean isEmpty() {
        if (calculateQuantityArray(true).length < 1){
            return true;
        }
        return false;
    }

    /*
    Name
    Address1
    Address2
    City
    State
    Zipcode
    Country
    Email
    PhoneNumber
    ExtraInformation
    ItemInformation
    Item1	Quantity1
    Item2	Quantity2
    Item3	Quantity3
    Item4	Quantity4
    Item5	Quantity5
    Item6	Quantity6
    Item7	Quantity7
    Item8	Quantity8
    Item9	Quantity9
    Item10	Quantity10
     */

    // todo: don't append the postcards for repeat customers
    public String toShipBobString() {
        System.out.println(firstName + " " + lastName);

        String[] quantityArray = calculateQuantityArray(true);

        String[] array = new String[]{
                "\"" + firstName + " " + lastName + "\"",
                "\"" + street1 + "\"",
                "\"" + street2 + "\"",
                "\"" + city + "\"",
                "\"" + state + "\"",
                "\"" + zip + "\"",
                "\"" + country + "\"",
                "\"" + email + "\"",
                "\"" + phoneNumber + "\"",

                "\"" + "\"",
                "\"" + email + "\"",

                // Item 1
                // Quantity 1 etc
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
        };

        for(int i = 0; i < quantityArray.length; i++){
            array[11+i] = quantityArray[i];
        }

        return StringUtils.join(array, ",");

    }

    public String toShipBobString(Set<String> previousCustomers) {
        System.out.println(firstName + " " + lastName);

        boolean isNewCustomer = !previousCustomers.contains(email);

        String[] quantityArray = calculateQuantityArray(isNewCustomer);

        String[] array = new String[]{
                "\"" + firstName + " " + lastName + "\"",
                "\"" + street1 + "\"",
                "\"" + street2 + "\"",
                "\"" + city + "\"",
                "\"" + state + "\"",
                "\"" + zip + "\"",
                "\"" + country + "\"",
                "\"" + email + "\"",
                "\"" + phoneNumber + "\"",

                "\"" + "\"",
                "\"" + email + "\"",

                // Item 1
                // Quantity 1 etc
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
                "","",
        };

        for(int i = 0; i < quantityArray.length; i++){
            array[11+i] = quantityArray[i];
        }

        return StringUtils.join(array, ",");

    }


    private String[] calculateQuantityArray(boolean isNewCustomer) {
        List<String> quantityList = new ArrayList<String>();

        if(silkCount > 0){
            quantityList.add("\"silk\"");
            quantityList.add(Integer.toString(silkCount));
        }
        if(silverCount > 0){
            quantityList.add("\"silver\"");
            quantityList.add(Integer.toString(silverCount));
        }
        if(goldCount > 0){
            quantityList.add("\"gold\"");
            quantityList.add(Integer.toString(goldCount));
        }
        if(bvCount > 0){
            quantityList.add("\"bv-clearing-kit\"");
            quantityList.add(Integer.toString(bvCount));
        }
        if(creamCount > 0){
            quantityList.add("\"cream\"");
            quantityList.add(Integer.toString(creamCount));
        }
        if(applicatorCount > 0){
            quantityList.add("\"applicator\"");
            quantityList.add(Integer.toString(applicatorCount));
        }
        if(assortedCount > 0){
            quantityList.add("\"assorted\"");
            quantityList.add(Integer.toString(assortedCount));
        }

        // we only have postcards in Chicago
        if(isNewCustomer && (silkCount + silverCount + goldCount + creamCount + assortedCount > 0)) {
//            quantityList.add("\"NeuEve Postcard\""); we ran out of NeuEve Postcards
//            quantityList.add("1");


//
//            quantityList.add("\"Vuvatech Postcard\""); we ran out of these
//            quantityList.add("1");
        }

        return quantityList.toArray(new String[0]);
    }

    public void addRefund(String referenceTxnId) {
        Map<String, Integer> shoppingCart = txnIdToItemIdAndQuantity.get(referenceTxnId);
        if(shoppingCart != null){
            for(Map.Entry<String, Integer> cartItem: shoppingCart.entrySet()){
                String itemId = cartItem.getKey();
                int itemQuantity = cartItem.getValue();

                switch (itemId){
                    case "silk":
                        this.silkCount = Math.max(0, this.silkCount - itemQuantity);
                        break;
                    case "silver":
                        this.silverCount = Math.max(0, this.silverCount - itemQuantity);
                        break;
                    case "gold":
                        this.goldCount = Math.max(0, this.goldCount - itemQuantity);
                        break;
                    case "cream":
                        this.creamCount = Math.max(0, this.creamCount - itemQuantity);
                        break;
                    case "bv-clearing-kit":
                        this.bvCount = Math.max(0, this.bvCount - itemQuantity);
                        break;
                    case "assorted":
                        this.assortedCount = Math.max(0, this.assortedCount - itemQuantity);
                        break;
                    case "applicator":
                        this.applicatorCount = Math.max(0, this.applicatorCount - itemQuantity);
                        break;
                }
            }
            txnIdToItemIdAndQuantity.remove(referenceTxnId);
        }
    }
}
