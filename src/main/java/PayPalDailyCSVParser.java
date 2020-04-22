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

public class PayPalDailyCSVParser {


    public static void main(String[] args) throws IOException {

        File source = new File("Download05-03-2018.CSV");

        CSVParser parser = CSVParser.parse(source, US_ASCII, CSVFormat.EXCEL.withHeader());

        System.out.println(parser.getHeaderMap());
        HashMap<String, CustomerRecord> emailToCustomerRecordMap = new HashMap<String, CustomerRecord>();

        PrintWriter writer = new PrintWriter("CustomerDataCleanJan.csv", "UTF-8");


        for (CSVRecord csvRecord : parser) {
            String from = csvRecord.get("From Email Address").toLowerCase();
            if (from.equals("neueve.suppositories@gmail.com")) {
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
            customerRecord.addPurchase(
                    Double.parseDouble(csvRecord.get("Gross")),
                    csvRecord.get("Type"),
                    new DateTime()
                            .withYear(Integer.parseInt(monthDateYear[2]))
                            .withMonthOfYear(Integer.parseInt(monthDateYear[0]))
                            .withDayOfMonth(Integer.parseInt(monthDateYear[1])),
                    csvRecord.get("Item Title"),
                    csvRecord.get("Item ID"),
                    Integer.parseInt(csvRecord.get("Quantity")),
                    csvRecord.get("Transaction ID")
            );
        }

        System.out.println(emailToCustomerRecordMap.size());

        writer.println("Email,Name,First Name,Last Name,Address,Lifetime Value,Num Purchases,Num Subs,Num BV,First Purchase,Last Purchase");

        for(CustomerRecord customerRecord : emailToCustomerRecordMap.values()){
            writer.println(customerRecord);
        }

        writer.close();

    }
}
