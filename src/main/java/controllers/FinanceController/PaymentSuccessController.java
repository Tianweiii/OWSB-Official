package controllers.FinanceController;

import javafx.fxml.Initializable;
import models.DTO.PaymentDTO;
import models.Datas.PurchaseOrder;
import models.Datas.ReceiptHeaderData;
import models.Users.FinanceManager;
import models.Users.User;
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
import java.util.*;
import java.util.List;

public class PaymentSuccessController implements Initializable {

    private PurchaseOrder currentPO = SessionManager.getCurrentPaymentPO();
    private FinanceMainController mainController;

    FinanceManager fm = SessionManager.getInstance().getFinanceManagerData();
    Navigator navigator = Navigator.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressBackToHome() {
//        mainController.goHome();
        navigator.navigate(navigator.getRouters("finance").getRoute("financeHome"));
    }

    public void pressPrintReceipt() throws ReflectiveOperationException, IOException, JRException {
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
        headerDataList.add(new ReceiptHeaderData("paid from", "10/123/123123", "date"));
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(headerDataList);

        JRBeanCollectionDataSource itemListDataSource = new JRBeanCollectionDataSource(rows);
        Map<String, Object> params = new HashMap<>();
        params.put("TABLE_DATA_SOURCE", itemListDataSource);

        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

        String pdfPath = "receipt.pdf";
        JasperExportManager.exportReportToPdfFile(print, pdfPath);

        // tootzejiat@gmail.com
        fm.sendReceipt(fm.getEmail(), MessageFormat.format("Payment receipt for {0}", currentPO.getPoID()), "receipt.pdf");
//        File pdfFile = new File(pdfPath);
//        if (pdfFile.exists()) {
//            if (Desktop.isDesktopSupported()) {
//                Desktop.getDesktop().open(pdfFile);
//            } else {
//                System.out.println("AWT Desktop is not supported on this platform.");
//            }
//        } else {
//            System.out.println("PDF file was not generated.");
//        }
    }
}
