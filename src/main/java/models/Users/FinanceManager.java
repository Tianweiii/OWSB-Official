package models.Users;

import models.Datas.Payment;
import models.Datas.PurchaseOrder;
import models.ModelInitializable;
import models.Utils.QueryBuilder;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

public class FinanceManager extends User {
//    private int FinanceId;

    final String paymentPath = "db/Payment.txt";
    final String[] paymentColumns = new String[]{"paymentID", "PO_ID", "userID", "paymentMethod", "amount", "createdAt", "paymentReference"};

    public FinanceManager(String user_id, String username, String email, String password, String position, int age, int role_id) {
        super(user_id, username, email, password, position, age, role_id);
    }

    public FinanceManager() {}

    public FinanceManager(String[] data) {
        super(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                Integer.parseInt(data[5]),
                Integer.parseInt(data[6])
        );
    }

    public ArrayList<HashMap<String, String>> getAllPayments() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        QueryBuilder<Payment> qb = new QueryBuilder<>(Payment.class);
        return qb.select().from(paymentPath).get();
    }

    public String createPayment(Payment payment) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        QueryBuilder<Payment> qb = new QueryBuilder<>(Payment.class);
        boolean res = qb
                .target(paymentPath)
                .values(payment.convertToArr())
                .create();

        return res ? "Successfully created payment" : "Something went wrong";
    }

    public String updatePayment(int paymentID, Payment payment) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        QueryBuilder<Payment> qb = new QueryBuilder<>(Payment.class);
        boolean res = qb
                .update(String.valueOf(paymentID), payment.convertToArr());
        return res ? MessageFormat.format("Successfully updated PaymentID {0}", paymentID) : "Something went wrong";
    }

    public boolean verifyInventoryUpdates(String invID) {

        return true;
    }

    public boolean approvePO(String PO_ID, String status) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        String path = "db/PurchaseOrder";
        HashMap<String, String> target = new HashMap<>();
        target.put("pr_order_status_id", status);
        QueryBuilder<PurchaseOrder> qb = new QueryBuilder<>(PurchaseOrder.class);
        return qb.target(path).update(PO_ID, target);
    }

    public static String sendReceipt(String receiverEmail, String subject, String filename) {
//        String senderEmail = "tootzejiat@gmail.com";
        String senderEmail = "tianweilow1003@gmail.com";
        String senderPassword = "wevy bwod yaai vnfy";
        String smtpHostServer = "smtp.gmail.com";

        String emailBody = "Test message 123";
       try {
           Properties props = System.getProperties();
           props.put("mail.smtp.host", smtpHostServer); // smtp host
           props.put("mail.smtp.port", "587"); // tls port
           props.put("mail.smtp.auth", "true"); // enable authentication
           props.put("mail.smtp.starttls.enable", "true"); // start tls

           Authenticator auth = new Authenticator() {
               @Override
               protected PasswordAuthentication getPasswordAuthentication() {
                   return new PasswordAuthentication(senderEmail, senderPassword);
               }
           };

           Session session = Session.getInstance(props, auth);
           InternetAddress senderDetails = new InternetAddress(senderEmail);

           session.setDebug(true);

           MimeMessage msg = new MimeMessage(session);

           // set headers and stuff
           msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
           msg.addHeader("format", "flowed");
           msg.addHeader("Content-Transfer-Encoding", "8bit");
           msg.setSubject(subject, "UTF-8");
           msg.setSentDate(new Date());

           // set recipients
           msg.setFrom(senderDetails);
           msg.setReplyTo(InternetAddress.parse(senderEmail, false));
           msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail, false));

           // the actual message part
           BodyPart messageBodyPart = new MimeBodyPart();
           messageBodyPart.setText(emailBody);
           Multipart multipart = new MimeMultipart();
           multipart.addBodyPart(messageBodyPart);

           // this is the attachment shit
           messageBodyPart = new MimeBodyPart();
//           String filename = "payment_report.pdf";
           FileDataSource source = new FileDataSource(filename);
           messageBodyPart.setDataHandler(new DataHandler(source));
           messageBodyPart.setFileName(filename);
           multipart.addBodyPart(messageBodyPart);

           // setting all the shit just now
           msg.setContent(multipart);

           // actually send the shit now
           Transport.send(msg);

           return "Email sent!";

       } catch (MessagingException e) {
           e.printStackTrace();
       }
        return "";
    }
}
