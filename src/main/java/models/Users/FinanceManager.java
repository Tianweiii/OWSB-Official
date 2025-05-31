package models.Users;

import controllers.NotificationController;
import models.Datas.Payment;
import models.Datas.PurchaseOrder;
import models.ModelInitializable;
import models.Utils.QueryBuilder;
import views.NotificationView;

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
import java.util.*;

public class FinanceManager extends User {
//    private int FinanceId;
    private String SenderEmail = "tianweilow1003@gmail.com";
    private String SenderPassword = "wevy bwod yaai vnfy";

    final String paymentPath = "db/Payment.txt";
    final String[] paymentColumns = new String[]{"paymentID", "poID", "userID", "paymentMethod", "amount", "createdAt", "paymentReference"};

    public FinanceManager(String user_id, String username, String email, String password, String position, int age, String role_id) {
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
                data[6]
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
        target.put("POStatus", status);
        QueryBuilder<PurchaseOrder> qb = new QueryBuilder<>(PurchaseOrder.class);
        return qb.target(path).update(PO_ID, target);
    }

    public void sendReceipt(String receiverEmail, String subject, String body, String filename) throws IOException {
        String senderEmail = SenderEmail;
        String senderPassword = SenderPassword;
        String smtpHostServer = "smtp.gmail.com";

        String emailBody = body;
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

           // this is the attachment
           messageBodyPart = new MimeBodyPart();
           FileDataSource source = new FileDataSource(filename);
           messageBodyPart.setDataHandler(new DataHandler(source));
           messageBodyPart.setFileName(filename);
           multipart.addBodyPart(messageBodyPart);

           // setting all the contents from just now
           msg.setContent(multipart);

           // actually send the email now
           Transport.send(msg);

           NotificationView notificationView = new NotificationView("Successfully sent to email, please check spam folder.", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
           notificationView.show();

       } catch (MessagingException | IOException e) {
           NotificationView notificationView = new NotificationView("Failed to send email.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
           notificationView.show();
           throw new RuntimeException(e);
       }
    }
}
