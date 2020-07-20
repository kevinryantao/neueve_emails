import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.US_ASCII;

// test comment

// This reads from two files.

// File 1 is the current Mailwizz Exported csv file

// File 2 is the new data to be merged in

// It will produce a file to be imported to Mailwizz

public class MailwizzCSVParserAndMerger {


    public static void main(String[] args) throws IOException {

        File mailwizzExport = new File("all-customers-subscribers-dv927wz51ca58.csv");

        File newDataToMerge = new File("CustomerDataCleanApr2_2020.csv");

        CSVParser mailwizzExportParser = CSVParser.parse(mailwizzExport, US_ASCII, CSVFormat.EXCEL.withHeader());

        CSVParser newDataParser = CSVParser.parse(newDataToMerge, US_ASCII, CSVFormat.EXCEL.withHeader());


        System.out.println(mailwizzExportParser.getHeaderMap());
        HashMap<String, CustomerRecord> emailToCustomerRecordMap = new HashMap<String, CustomerRecord>();

        HashMap<String, CustomerRecord> updatedEmailToCustomerRecordMap = new HashMap<String, CustomerRecord>();

        PrintWriter writer = new PrintWriter("mergedCustomerDataApril2_2020.csv", "UTF-8");

        int newCust = 0;
        int oldCust = 0;

        for (CSVRecord csvRecord : mailwizzExportParser) {

            String from = csvRecord.get("EMAIL").toLowerCase();

            CustomerRecord customerRecord = emailToCustomerRecordMap.get(from);
            if (customerRecord == null) {


                DateTime subscribeStartDate = null;
                String subscribe_start_date = csvRecord.get("SUBSCRIBE_START_DATE");
                if (!subscribe_start_date.isEmpty()){
                    subscribeStartDate = DateTime.parse(subscribe_start_date);
                }

                customerRecord = new CustomerRecord(csvRecord.get("EMAIL").toLowerCase(),
                        csvRecord.get("FNAME"),
                        csvRecord.get("LNAME"),
                        csvRecord.get("STREET"),
                        csvRecord.get("ZIP"),
                        csvRecord.get("CITY"),
                        csvRecord.get("STATE"),
                        csvRecord.get("COUNTRY"),
                        Integer.parseInt(csvRecord.get("CHECKOUT_COUNT")),
                        Integer.parseInt(csvRecord.get("CART_ITEM_COUNT")),
                        Integer.parseInt(csvRecord.get("IS_SUBSCRIBER")) == 1,
                        subscribeStartDate,
                        Integer.parseInt(csvRecord.get("SILK_COUNT")),
                        Integer.parseInt(csvRecord.get("SILVER_COUNT")),
                        Integer.parseInt(csvRecord.get("GOLD_COUNT")),
                        Integer.parseInt(csvRecord.get("BV_COUNT")),
                        Integer.parseInt(csvRecord.get("CREAM_COUNT")),
                        Integer.parseInt(csvRecord.get("APPLICATOR_COUNT")),
                        Integer.parseInt(csvRecord.get("ASSORTED_COUNT")),
                        csvRecord.get("LAST_TXN_ID"),
                        DateTime.parse(csvRecord.get("FIRST_PURCHASE_DATE")),
                        DateTime.parse(csvRecord.get("LAST_PURCHASE_DATE"))
                        );
                emailToCustomerRecordMap.put(from, customerRecord);
            }
        }

        for (CSVRecord csvRecord : newDataParser) {

            String email = csvRecord.get("Email").toLowerCase();


            DateTime subscribeStartDate = null;
            String subscribe_start_date = csvRecord.get("Subscribe Start Date");
            if (!subscribe_start_date.isEmpty()){
                subscribeStartDate = DateTime.parse(subscribe_start_date);
            }


            CustomerRecord customerRecord = new CustomerRecord(email,
                    csvRecord.get("First Name"),
                    csvRecord.get("Last Name"),
                    csvRecord.get("Street"),
                    csvRecord.get("Zip"),
                    csvRecord.get("City"),
                    csvRecord.get("State"),
                    csvRecord.get("Country"),
                    Integer.parseInt(csvRecord.get("Checkout Count")),
                    Integer.parseInt(csvRecord.get("Cart Item Count")),
                    Integer.parseInt(csvRecord.get("Is Subscriber")) == 1,
                    subscribeStartDate,
                    Integer.parseInt(csvRecord.get("Silk Count")),
                    Integer.parseInt(csvRecord.get("Silver Count")),
                    Integer.parseInt(csvRecord.get("Gold Count")),
                    Integer.parseInt(csvRecord.get("BV Count")),
                    Integer.parseInt(csvRecord.get("Cream Count")),
                    Integer.parseInt(csvRecord.get("Applicator Count")),
                    Integer.parseInt(csvRecord.get("Assorted Count")),
                    csvRecord.get("Last Txn Id"),
                    DateTime.parse(csvRecord.get("First Purchase Date")),
                    DateTime.parse(csvRecord.get("Last Purchase Date"))
            );

            CustomerRecord possiblePreviousRecord = emailToCustomerRecordMap.get(email);

            if(possiblePreviousRecord != null){
                // record already exists
                oldCust++;
                possiblePreviousRecord.mergeWithOtherCustomerRecord(customerRecord);
                updatedEmailToCustomerRecordMap.put(email,possiblePreviousRecord);
            } else {
                newCust++;
                updatedEmailToCustomerRecordMap.put(email, customerRecord);
            }
        }

        System.out.println(updatedEmailToCustomerRecordMap.size());
        System.out.println("Old Customers: " + oldCust);
        System.out.println("New Customers: " + newCust);

        writer.println("Email,First Name,Last Name," +
                "Street,Zip,City,State,Country," +
                "Checkout Count,Cart Item Count,Is Subscriber,Subscribe Start Date," +
                "Silk Count,Silver Count,Gold Count,BV Count,Cream Count,Applicator Count,Assorted Count,Last Txn Id," +
                "First Purchase Date,Last Purchase Date,Phone");

        for(CustomerRecord customerRecord : updatedEmailToCustomerRecordMap.values()){
            writer.println(customerRecord);
        }

        writer.close();

    }
}
