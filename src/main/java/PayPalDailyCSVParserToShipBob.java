import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

/*
* Inputs:
* File Named "LastUploadedRecord.txt" - Contains a datetime and name and email of person. Transaction ID?
* File Named "DownloadXX-XX-XXXX.CSV" - Contains about 1 day's worth of payment data from PayPal.
* File Named "AllPreviousCustomers.txt" - Contains about all emails of old customers.
*
* Outputs:
* Overwrite file Named "LastUploadedRecord.txt" with new last person's datetime, name, email and TXN ID
* File named NeuEveXX-XX-XXXX.csv
* File named WarningsXX-XX-XXXX.txt for any inconsistent data that needs to be manually checked
*
* Pseudocode:
* 1. Parse the LastUploadedRecord.txt to load in last person from yesterday
* 2. Parse DownloadXX-XX-XXXX.CSV
*   For Each Record:
*     Check the time. If it's before the last uploaded record, skip it.
*
*     Clean the transaction-
*       - Replace ***Change to Silver*** to the actual one
*       - Replace silk-ca to silk
*       - Replace 1 - silk, 2 - silver
*       - Check to see if the quantity was left blank or other weird stuff
*     Add the transaction
*     If it's a Refund, remove look it up based on Reference Txn ID. The item id of a refund can't be trusted.
*
*     Save the latest record as the new last uploaded record.
*
*
*     Notes: Key based on Mailing Address. NOT Email. If there's a mismatch give a warning.
*
* 3. Result will have a condensed map of Keys to CustomerRecords
* 4. Add Vuvatech postcard + NeuEve Postcard to non-BV Clearing Kit.
* 5. Customer Record toShipBob() returns a CSV String.
*
* todo: Throw some kind of warning when it's the same customer but different address.
* todo: Skip when PayPal data is incomplete
* todo: Mark when a Canada order is a subscription at 49.95
 *
* */

public class PayPalDailyCSVParserToShipBob {


    private static final DateTimeFormatter PAYPAL_DATETIME_FORMAT = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss zzz");

    public static void main(String[] args) throws Exception {

        Scanner allPreviousCustomers = new Scanner(new File("AllPreviousCustomers.txt"));
        Set<String> prevCustomers = new HashSet<String>();
        while(allPreviousCustomers.hasNextLine()){
            prevCustomers.add(allPreviousCustomers.nextLine().toLowerCase());
        }
        allPreviousCustomers.close();

        System.out.println(DateTime.now());

        Scanner lastUploadedRecord = new Scanner(new File("LastUploadedRecord.txt"));
        DateTime lastUploadedDateTime = DateTime.now().minusYears(1);
        if(lastUploadedRecord.hasNextLine()){
            lastUploadedDateTime = DateTime.parse(lastUploadedRecord.nextLine(), PAYPAL_DATETIME_FORMAT);
        } else {
            throw new Exception("No Last Uploaded Date!");
        }
        lastUploadedRecord.close();

        System.out.println(lastUploadedDateTime.toString(PAYPAL_DATETIME_FORMAT));

        File source = new File("Download02-07-2022.CSV");
        PrintWriter writer = new PrintWriter("NeuEve02-07-2022.csv", "UTF-8");

        CSVParser parser = CSVParser.parse(source, UTF_8, CSVFormat.EXCEL.withHeader());

        System.out.println(parser.getHeaderMap());
        HashMap<String, CustomerRecord> emailToCustomerRecordMap = new HashMap<String, CustomerRecord>();

        DateTime newLastUploadedDateTime = lastUploadedDateTime;

        for (CSVRecord csvRecord : parser) {

            // Don't upload the old stuff
            String dateString = csvRecord.get(0);
            String timeString = csvRecord.get("Time");
            String timeZoneString = csvRecord.get("TimeZone");
            DateTime uploadedDateTime = DateTime.parse(dateString + " " + timeString + " " + timeZoneString, PAYPAL_DATETIME_FORMAT);
            if(!uploadedDateTime.isAfter(lastUploadedDateTime)){
                continue;
            }

            if(csvRecord.get("Item Title") != null && csvRecord.get("Item ID") != null){
                if(uploadedDateTime.isAfter(newLastUploadedDateTime)){
                    newLastUploadedDateTime = uploadedDateTime;
                }
            }

            String type = csvRecord.get("Type");

            String from = csvRecord.get("From Email Address").toLowerCase();
            if (from.equals("")) {
                continue;
            }
            if (from.equals("neueve.suppositories@gmail.com")) {
                if ("Payment Refund".equals(type)){
                    String refundEmailAddress = csvRecord.get("To Email Address");
                    String referenceTxnId = csvRecord.get("Reference Txn ID");
                    CustomerRecord customerRecord = emailToCustomerRecordMap.get(refundEmailAddress);
                    if (customerRecord != null){
                        customerRecord.addRefund(referenceTxnId);
                    }
                }
                continue;
            }

            String customNumber = csvRecord.get("Custom Number");
            if ("Shopify".equals(customNumber)){
                continue;
            }

            if (!"Subscription Payment".equals(type) && !"Website Payment".equals(type) && !"Shopping Cart Item".equals(type) && !"eBay Auction Payment".equals(type)){
                continue;
            }

            CustomerRecord customerRecord = emailToCustomerRecordMap.get(from);
            if (customerRecord == null) {
                customerRecord = new CustomerRecord(csvRecord.get("Name"),
                        from,
                        csvRecord.get("Shipping Address"),
                        csvRecord.get("Address Line 1"),
                        csvRecord.get("Address Line 2/District/Neighborhood"),
                        csvRecord.get("Town/City"),
                        csvRecord.get("State/Province/Region/County/Territory/Prefecture/Republic"),
                        csvRecord.get("Zip/Postal Code"),
                        csvRecord.get("Country"),
                        csvRecord.get("Contact Phone Number"));
                emailToCustomerRecordMap.put(from, customerRecord);
            }
            String[] monthDateYear = csvRecord.get(0).split("/");

            // If quantity is empty we can deduce that it is 1
            String quantity = csvRecord.get("Quantity");
            if(quantity.isEmpty()){
                quantity = "1";
            }

            // If ItemTitle and ItemID are empty, we print a warning with the persons name
            if(csvRecord.get("Item Title").isEmpty() && csvRecord.get("Item ID").isEmpty()){
                System.out.println("*** WARNING ***");
                System.out.println(csvRecord.get("Name") + " " + " has an empty item title and item ID");
                System.out.println("*** WARNING ***");
            }

            NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
            nf.parse(csvRecord.get("Gross")).doubleValue();

            customerRecord.addPurchase(
                    nf.parse(csvRecord.get("Gross")).doubleValue(),
                    csvRecord.get("Type"),
                    new DateTime()
                            .withYear(Integer.parseInt(monthDateYear[2]))
                            .withMonthOfYear(Integer.parseInt(monthDateYear[0]))
                            .withDayOfMonth(Integer.parseInt(monthDateYear[1])),
                    csvRecord.get("Item Title"),
                    csvRecord.get("Item ID"),
                    Integer.parseInt(quantity),
                    csvRecord.get("Transaction ID")
            );
        }

        System.out.println(emailToCustomerRecordMap.size());






        writer.println("Name,Address1,Address2,City,State,Zipcode,Country,Email,PhoneNumber,ExtraInformation,ItemInformation,Item1,Quantity1,Item2,Quantity2,Item3,Quantity3,Item4,Quantity4,Item5,Quantity5,Item6,Quantity6,Item7,Quantity7,Item8,Quantity8,Item9,Quantity9,Item10,Quantity10");

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

            int totalCustomers = 0;
            int newCustomers = 0;
            int returnCustomers = 0;

        for(CustomerRecord customerRecord : emailToCustomerRecordMap.values()){
            if (!customerRecord.isEmpty()){
                writer.println(customerRecord.toShipBobString(prevCustomers));

                totalCustomers++;
                if(prevCustomers.contains(customerRecord.email)){
                    returnCustomers++;
                } else {
                    newCustomers++;
                }

                prevCustomers.add(customerRecord.email);
            }
        }

        // add the empty customers at the end. EDIT disable this because PayPal is no longer giving us empty data
//        for(CustomerRecord customerRecord : emailToCustomerRecordMap.values()){
//            if (customerRecord.isEmpty()){
//                writer.println(customerRecord.toShipBobString(prevCustomers));
//
//                totalCustomers++;
//                if(prevCustomers.contains(customerRecord.email)){
//                    returnCustomers++;
//                } else {
//                    newCustomers++;
//                }
//
//                prevCustomers.add(customerRecord.email);
//            }
//        }

        writer.close();


        System.out.println(DateTime.now());

        System.out.println("---");
        System.out.println("Total Customers : " + totalCustomers);
        System.out.println("New Customers : " + newCustomers);
        System.out.println("Return Customers : " + returnCustomers);

        int subscriberCount = getSubscribers(emailToCustomerRecordMap.values());
        System.out.println("Subscriber Orders : " + subscriberCount);
        System.out.println("A la carte Orders : " + (totalCustomers - subscriberCount));
        System.out.println("---");

        Map<String, Integer> orderCounts = orderCounts(emailToCustomerRecordMap.values());
        for(String sku: orderCounts.keySet()){
            System.out.println(sku + " : " + orderCounts.get(sku));
        }

        PrintWriter datewriter = new PrintWriter("LastUploadedRecord.txt");
        datewriter.println(newLastUploadedDateTime.toString(PAYPAL_DATETIME_FORMAT));
        datewriter.close();

        PrintWriter emailwriter = new PrintWriter("AllPreviousCustomers.txt");
        for(String email: prevCustomers){
            emailwriter.println(email);
        }
        emailwriter.close();
    }

    private static int getSubscribers(Collection<CustomerRecord> customerRecords){
        int subscribers = 0;
        for(CustomerRecord customerRecord: customerRecords){
            if(customerRecord.isSubscriber){
                subscribers++;
            }
        }
        return subscribers;
    }

    private static Map<String, Integer> orderCounts(Collection<CustomerRecord> customerRecords){
        Map<String, Integer> skusList = new TreeMap<String, Integer>();
        skusList.put("silk",0);
        skusList.put("silver",0);
        skusList.put("gold",0);
        skusList.put("cream",0);
        skusList.put("applicator",0);
        skusList.put("assorted",0);
        skusList.put("bv-clearing-kit",0);
        skusList.put("finisher",0);
        skusList.put("sea_buckthorn_60", 0);
        for(CustomerRecord customerRecord: customerRecords) {
            skusList.put("silk", skusList.get("silk") + customerRecord.silkCount);
            skusList.put("silver", skusList.get("silver") + customerRecord.silverCount);
            skusList.put("gold", skusList.get("gold") + customerRecord.goldCount);
            skusList.put("cream", skusList.get("cream") + customerRecord.creamCount);
            skusList.put("applicator", skusList.get("applicator") + customerRecord.applicatorCount);
            skusList.put("assorted", skusList.get("assorted") + customerRecord.assortedCount);
            skusList.put("bv-clearing-kit", skusList.get("bv-clearing-kit") + customerRecord.bvCount);
            skusList.put("finisher", skusList.get("finisher") + customerRecord.finisherCount);
            skusList.put("sea_buckthorn_60", skusList.get("sea_buckthorn_60") + customerRecord.seaBuckthorn60Count);
        }
        return skusList;
    }
}
