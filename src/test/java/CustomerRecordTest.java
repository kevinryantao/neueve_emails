import org.joda.time.DateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRecordTest {

    @org.junit.jupiter.api.Test
    void addPurchase() {
        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "silk",
                1,
                "2DU135542F264444W");

        // Ignore the Shopping Cart Items for Subscriptions
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve Subscription",
                "silk",
                55,
                "2DU135542F264444W");

        // ignore "Website Payments"
        customerRecord.addPurchase(49,
                "Website Payment",
                new DateTime(),
                "NeuEve Cream (2-3 month supply), NeuEve Gold Formula (1 month supply), Suppository Applicator, Reusable",
                "cream, gold, applicator",
                57,
                "2DU135542F264444W");


        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve Silver Formula (1 month supply)",
                "silver",
                2,
                "2DU135542F264444W");
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve Gold Formula (1 month supply)",
                "gold",
                3,
                "2DU135542F264444W");
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve BV Clear",
                "bv-clearing-kit",
                4,
                "2DU135542F264444W");
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "Suppository Applicator, Reusable",
                "applicator",
                5,
                "2DU135542F264444W");
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve Assorted Formulas (1 month supply)",
                "assorted",
                6,
                "2DU135542F264444W");
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve Cream (2-3 month supply)",
                "cream",
                7,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silkCount);
        assertEquals(2, customerRecord.silverCount);
        assertEquals(3, customerRecord.goldCount);
        assertEquals(4, customerRecord.bvCount);
        assertEquals(5, customerRecord.applicatorCount);
        assertEquals(6, customerRecord.assortedCount);
        assertEquals(7, customerRecord.creamCount);
    }

    @org.junit.jupiter.api.Test
    void addPurchase3Stars() {
        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve ***Change to Silver***",
                "1",
                1,
                "2DU135542F264444W");

        // Ignore the Shopping Cart Items for Subscriptions
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve ***Change to Silver***",
                "1",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silverCount);
        assertEquals(0, customerRecord.silkCount);

        customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "*** Change to Gold***",
                "silk",
                1,
                "2DU135542F264444W");

        // Ignore the Shopping Cart Items for Subscriptions
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "*** Change to Gold***",
                "silk",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.goldCount);
        assertEquals(0, customerRecord.silkCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "Change to ****Silk****",
                "silver",
                1,
                "2DU135542F264444W");

        // Ignore the Shopping Cart Items for Subscriptions
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "Change to ****Silk****",
                "silver",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silkCount);
    }


    @org.junit.jupiter.api.Test
    void addPurchase3StarIgnoreOtherChanges() {
        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve ***Change email to*** jamesandlinda79@gmail.com",
                "gold",
                1,
                "2DU135542F264444W");

        // Ignore the Shopping Cart Items for Subscriptions
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve ***Change email to*** jamesandlinda79@gmail.com",
                "gold",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.goldCount);


        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve***will not send for Aug 22, 2019***",
                "silver",
                1,
                "2DU135542F264444W");

        // Ignore the Shopping Cart Items for Subscriptions
        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve***will not send for Aug 22, 2019***",
                "silver",
                1,
                "2DU135542F264444W");


    }

    @org.junit.jupiter.api.Test
    void addPurchaseCanada() {
        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "silk-ca",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silkCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "silver-ca",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silverCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "gold-ca",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.goldCount);
    }


    @org.junit.jupiter.api.Test
    void addPurchaseLegacyNumbers() {
        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "1",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silkCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "2",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silverCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "3",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.goldCount);
    }


    @org.junit.jupiter.api.Test
    void addPurchase4x() {
        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "4 x NeuEve Silk",
                "silk x 4",
                1,
                "2DU135542F264444W");

        assertEquals(4, customerRecord.silkCount);

        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "4 x NeuEve BV Clear",
                "bv-clearing-kit x 4",
                1,
                "2DU135542F264444W");

        assertEquals(4, customerRecord.bvCount);

        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "4 x NeuEve Silver",
                "silver x 4",
                1,
                "2DU135542F264444W");

        assertEquals(4, customerRecord.silverCount);


        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "4 x NeuEve Gold",
                "gold x 4",
                1,
                "2DU135542F264444W");

        assertEquals(4, customerRecord.goldCount);


        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "4 x NeuEve Cream",
                "cream x 4",
                1,
                "2DU135542F264444W");

        assertEquals(4, customerRecord.creamCount);


    }


    // There's no way to do a refund based on the Transaction ID here. Need to do some other path. Or keep a record here.
    // I think we should keep a record here.
    @org.junit.jupiter.api.Test
    void addPurchaseRefunds() {
        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "bv-clearing-kit x 4",
                1,
                "ABCDE");

        assertEquals(4, customerRecord.bvCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "silk",
                1,
                "2DU135542F264444W");

        assertEquals(1, customerRecord.silkCount);

        customerRecord.addRefund("2DU135542F264444W");
        assertEquals(4, customerRecord.bvCount);

        assertEquals(0, customerRecord.silkCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "silver",
                2,
                "2DU135542F264444W2");

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "cream",
                3,
                "2DU135542F264444W2");

        assertEquals(2, customerRecord.silverCount);
        assertEquals(3, customerRecord.creamCount);

        customerRecord.addRefund("2DU135542F264444W2");

        assertEquals(0, customerRecord.silverCount);
        assertEquals(0, customerRecord.creamCount);

        assertEquals(4, customerRecord.bvCount);

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "bv-clearing-kit",
                3,
                "ABCDEFG");

        assertEquals(7, customerRecord.bvCount);

        customerRecord.addRefund("ABCDE");

        assertEquals(3, customerRecord.bvCount);

        customerRecord.addRefund("ABCDE");

        assertEquals(3, customerRecord.bvCount);

        customerRecord.addRefund("ABCDEFG");

        assertEquals(0, customerRecord.bvCount);
    }


    @org.junit.jupiter.api.Test
    void mergeWithOtherCustomerRecord() {
    }

    @org.junit.jupiter.api.Test
    void testToString() {
    }

    @org.junit.jupiter.api.Test
    void toShipBobString() {

        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Subscription Payment",
                new DateTime(),
                "NeuEve Subscription",
                "silver",
                1,
                "2DU135542F264444W");

        assertEquals("\"Kevin Tao\",\"123 Fake Street\",\"Apt B\",\"Faketown\",\"AK\",\"12345\",\"USA\",\"kevin@fakemail.com\",\"1234566789\",\"\",\"kevin@fakemail.com\",\"silver\",1,\"NeuEve Postcard\",1,\"Vuvatech Postcard\",1,,,,,,,,,,,,,,",
                customerRecord.toShipBobString());

        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "Suppository Applicator, Reusable",
                "applicator",
                1,
                "1C382714DR0693311");

        assertEquals("\"Kevin Tao\",\"123 Fake Street\",\"Apt B\",\"Faketown\",\"AK\",\"12345\",\"USA\",\"kevin@fakemail.com\",\"1234566789\",\"\",\"kevin@fakemail.com\",\"silver\",1,\"applicator\",1,\"NeuEve Postcard\",1,\"Vuvatech Postcard\",1,,,,,,,,,,,,",
                customerRecord.toShipBobString());

    }

    @org.junit.jupiter.api.Test
    void toShipBobStringBV() {

        CustomerRecord customerRecord = createCustomerRecord();

        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "NeuEve BV Clear",
                "bv-clearing-kit",
                1,
                "2DU135542F264444W");

        assertEquals("\"Kevin Tao\",\"123 Fake Street\",\"Apt B\",\"Faketown\",\"AK\",\"12345\",\"USA\",\"kevin@fakemail.com\",\"1234566789\",\"\",\"kevin@fakemail.com\",\"bv-clearing-kit\",1,,,,,,,,,,,,,,,,,,",
                customerRecord.toShipBobString());

        customerRecord.addPurchase(49,
                "Shopping Cart Item",
                new DateTime(),
                "Suppository Applicator, Reusable",
                "applicator",
                1,
                "1C382714DR0693311");

        assertEquals("\"Kevin Tao\",\"123 Fake Street\",\"Apt B\",\"Faketown\",\"AK\",\"12345\",\"USA\",\"kevin@fakemail.com\",\"1234566789\",\"\",\"kevin@fakemail.com\",\"bv-clearing-kit\",1,\"applicator\",1,,,,,,,,,,,,,,,,",
                customerRecord.toShipBobString());

    }

    private CustomerRecord createCustomerRecord() {
        return new CustomerRecord("Kevin Tao",
                "kevin@fakemail.com",
                "Kevin Ryan, Tao, 123 Fake Street, Cary, IL, 60013, United States",
                "123 Fake Street",
                "Apt B",
                "Faketown",
                "AK",
                "12345",
                "USA",
                "1234566789");
    }

}