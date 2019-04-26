import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

// test comment

import static java.nio.charset.StandardCharsets.*;

public class PayPalCSVParser {


    public static void main(String[] args) throws IOException {

        File source = new File("Last6MoToApril252019.CSV");

        CSVParser parser = CSVParser.parse(source, US_ASCII, CSVFormat.EXCEL.withHeader());

        System.out.println(parser.getHeaderMap());
        HashMap<String, CustomerRecord> emailToCustomerRecordMap = new HashMap<String, CustomerRecord>();

        PrintWriter writer = new PrintWriter("CustomerDataCleanApr25.csv", "UTF-8");

        for (CSVRecord csvRecord : parser) {
            String from = csvRecord.get("From Email Address").toLowerCase();
            String type = csvRecord.get("Type");
            if (from.equals("neueve.suppositories@gmail.com") ||
                    from.equals("") ||
                    type.equals("General Currency Conversion") ||
                    type.equals("Chargeback Reversal") ||
                    type.equals("Cancellation of Hold for Dispute Resolution") ||
                    type.equals("Payment Refund") ||
                    type.equals("General Payment")) {
                continue;
            }
            CustomerRecord customerRecord = emailToCustomerRecordMap.get(from);
            if (customerRecord == null) {
                customerRecord = new CustomerRecord(csvRecord.get("Name"),
                        from,
                        csvRecord.get("Shipping Address"),
                        csvRecord.get("Address Line 1") + csvRecord.get("Address Line 2/District/Neighborhood"),
                        csvRecord.get("Town/City"),
                        csvRecord.get("State/Province/Region/County/Territory/Prefecture/Republic"),
                        csvRecord.get("Zip/Postal Code"),
                        csvRecord.get("Country"));
                emailToCustomerRecordMap.put(from, customerRecord);
            }
            String[] monthDateYear = csvRecord.get(0).split("/");
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

        writer.println("Email,First Name,Last Name," +
                "Street,Zip,City,State,Country," +
                "Checkout Count,Cart Item Count,Is Subscriber,Subscribe Start Date," +
                "Silk Count,Silver Count,Gold Count,BV Count,Cream Count,Applicator Count,Assorted Count,Last Txn Id," +
                "First Purchase Date,Last Purchase Date");

        for(CustomerRecord customerRecord : emailToCustomerRecordMap.values()){
            writer.println(customerRecord);
        }

        writer.close();

    }
}
