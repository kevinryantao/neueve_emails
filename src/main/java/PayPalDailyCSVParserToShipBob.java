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
import java.util.HashMap;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.US_ASCII;

/*
* Inputs:
* File Named "LastUploadedRecord.txt" - Contains a datetime and name and email of person. Transaction ID?
* File Named "DownloadXX-XX-XXXX.CSV" - Contains about 1 day's worth of payment data from PayPal.
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
* todo: Create a list of all repeat customers
* todo: Throw some kind of warning when it's the same customer but different address.
* todo: Non ASCII characters like Acela Gómez
* todo: pull name from Shipping Address
 *
* */

public class PayPalDailyCSVParserToShipBob {


    private static final DateTimeFormatter PAYPAL_DATETIME_FORMAT = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss zzz");

    public static void main(String[] args) throws Exception {

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

        File source = new File("Download04-23-2020.CSV");
        PrintWriter writer = new PrintWriter("NeuEve04-23-2020.csv", "UTF-8");

        CSVParser parser = CSVParser.parse(source, US_ASCII, CSVFormat.EXCEL.withHeader());

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
            customerRecord.addPurchase(
                    Double.parseDouble(csvRecord.get("Gross")),
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

        for(CustomerRecord customerRecord : emailToCustomerRecordMap.values()){
            if (!customerRecord.isEmpty()){
                writer.println(customerRecord.toShipBobString());
            }
        }

        writer.close();


        System.out.println(DateTime.now());

        PrintWriter datewriter = new PrintWriter("LastUploadedRecord.txt");
        datewriter.println(newLastUploadedDateTime.toString(PAYPAL_DATETIME_FORMAT));
        datewriter.close();
    }
}
