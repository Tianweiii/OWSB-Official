package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import models.DTO.PaymentDTO;
import models.Datas.Payment;
import models.Datas.PurchaseOrder;
import models.Datas.ReceiptHeaderData;
import models.Users.FinanceManager;
import models.Users.User;
import models.Utils.FileIO;
import models.Utils.Navigator;
import models.Utils.SessionManager;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PaymentSuccessController implements Initializable {

    @FXML
    private Text amountPaid;
    @FXML
    private Text email;
    @FXML
    private Text paymentReference;
    @FXML
    private Text paymentType;
    @FXML
    private Text receipientName;

    private PurchaseOrder currentPO = SessionManager.getCurrentPaymentPO();
    private FinanceMainController mainController;
    private Payment payment;
    private FinanceManager user;

    FinanceManager fm = SessionManager.getInstance().getFinanceManagerData();
    Navigator navigator = Navigator.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setMainData();
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    private void setMainData() throws IOException, ReflectiveOperationException {
        // Payment details
        payment = FileIO.getObjectFromID(Payment.class, "Payment", 1, currentPO.getPoID());
        System.out.println(payment.toString());
        user = FileIO.getIDsAsObject(FinanceManager.class, "User", payment.getUserID());

        amountPaid.setText("RM " + payment.getAmount());
        email.setText(user.getEmail());
        paymentReference.setText(payment.getPaymentReference());
        paymentType.setText(payment.getPaymentMethod());
        receipientName.setText(user.getName());
    }

    public void onPressBackToHome() {
        navigator.navigate(navigator.getRouters("finance").getRoute("financeHome"));
    }

    public void pressPrintReceipt() throws ReflectiveOperationException, IOException, JRException {
        LocalDateTime now = LocalDateTime.now();  // Get current date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        Map<String, List<PaymentDTO>> paymentItems = SessionManager.getCurrentPaymentPO().getPurchaseItemList();

        // jasperreport keys: descripton, unitPrice, quantity, total
        List<Map<String, ?>> rows = new ArrayList<>();

        for (var entry : paymentItems.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            for (PaymentDTO i : entry.getValue()) {
                row.put("description", i.getItemName());
                row.put("unitPrice", i.getUnitPrice());
                row.put("quantity", i.getQuantity());
                row.put("total", i.getAmount());

                rows.add(row);
            }
        }

        JasperReport report = (JasperReport) JRLoader.loadObjectFromFile("src/main/resources/Jasper/Receipt.jasper");

        List<ReceiptHeaderData> headerDataList = new ArrayList<>();
        headerDataList.add(new ReceiptHeaderData(SessionManager.getInstance().getFinanceManagerData().getName(), payment.getPaymentReference(), formattedNow));
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(headerDataList);

        JRBeanCollectionDataSource itemListDataSource = new JRBeanCollectionDataSource(rows);
        Map<String, Object> params = new HashMap<>();
        params.put("TABLE_DATA_SOURCE", itemListDataSource);

        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

        String pdfPath = "tmp/receipt" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
        JasperExportManager.exportReportToPdfFile(print, pdfPath);

        File pdfFile = new File(pdfPath);
        if (pdfFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("AWT Desktop is not supported on this platform.");
            }
        } else {
            System.out.println("PDF file was not generated.");
        }

        // tootzejiat@gmail.com
        fm.sendReceipt("leeaikyen@gmail.com", MessageFormat.format("Payment receipt for {0}", currentPO.getPoID()), "Receipt for payment", pdfPath);
    }
}
