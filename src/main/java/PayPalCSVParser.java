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

import static java.nio.charset.StandardCharsets.*;

public class PayPalCSVParser {


    public static void main(String[] args) throws IOException {

        File source = new File("DownloadJan26toJan28.CSV");

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
                customerRecord = new CustomerRecord(csvRecord.get("Name"), from, csvRecord.get("Shipping Address"));
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
                    csvRecord.get("Item Title"));
        }

        System.out.println(emailToCustomerRecordMap.size());

        writer.println("Email,Name,First Name,Last Name,Address,Lifetime Value,Num Purchases,Num Subs,Num BV,First Purchase,Last Purchase");

        for(CustomerRecord customerRecord : emailToCustomerRecordMap.values()){
            writer.println(customerRecord);
        }

        writer.close();

    }
}
