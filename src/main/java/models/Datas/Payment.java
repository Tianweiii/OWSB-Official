package models.Datas;

import models.Utils.Helper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Payment {

	public enum PaymentMethod {
		Bank, TnG
	};

	private final String paymentID;
	private final PaymentMethod paymentMethod;
	private double amount;
	private final String createdAt;
	private final String PO_ID;
	private final String userID;
	private final String paymentReference;

	public Payment(PaymentMethod method, double amount, String PO_ID, String userID) {
		this.paymentMethod = method;
		this.amount = amount;
		this.PO_ID = PO_ID;
		this.userID = userID;

		// TODO: get latest row count
		int count = 1;

		this.paymentID = MessageFormat.format("PY{0}", count); // TODO: change to fetch row count
		this.createdAt = LocalDateTime.now().toString();
		this.paymentReference = generatePaymentReference(count);
	}

	private String generatePaymentReference(int hashKey) {
		String prefix = "REF";
		String temp = String.valueOf(hashKey);
		String hashCode = Helper.MD5_Hashing(temp).substring(0, 10);

		return MessageFormat.format("{0}-{1}", prefix, hashCode);
	}

	public String getPaymentID() {
		return paymentID;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public double getAmount() {
		return amount;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getPO_ID() {
		return PO_ID;
	}

	public String getUserID() {
		return userID;
	}

	public String getPaymentReference() {
		return paymentReference;
	}

	public static void generatePaymentReportPDF() {
		try {
			List<Map<String, ?>> rows = new ArrayList<>();

			BufferedReader reader = new BufferedReader(new FileReader("src/main/java/db/Payment.txt"));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");

				Map<String, Object> row = new HashMap<>();
				row.put("paymentID", tokens[0]);
				row.put("name", tokens[1]);

				row.put("amount", Double.parseDouble(tokens[2].trim()));

				row.put("paymentMethod", tokens[3].trim());
				row.put("PO_ID", tokens[4]);
				row.put("paymentReference", tokens[5]);

				rows.add(row);
			}
			reader.close();

			JasperReport report = (JasperReport) JRLoader.loadObjectFromFile("src/main/resources/Jasper/TestReport.jasper");

			// creator data
			List<Test> creatorDataList = new ArrayList<>();
			creatorDataList.add(new Test("Test Creator", "10/10/2000"));
			JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(creatorDataList);

			// table data
			JRBeanCollectionDataSource paymentDataSource = new JRBeanCollectionDataSource(rows);
			Map<String, Object> params = new HashMap<>();
			params.put("TABLE_DATA_SOURCE", paymentDataSource);

			// fill pdf little shit
			JasperPrint print = JasperFillManager.fillReport(report, params, datasource);

			// export that shit
			String pdfPath = "payment_report.pdf";
			JasperExportManager.exportReportToPdfFile(print, pdfPath);

			// open that shit
			File pdfFile = new File(pdfPath);
			if (pdfFile.exists()) {
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().open(pdfFile);
				} else {
					System.out.println("AWT Desktop is not supported on this platform.");
				}
			} else {
				System.out.println("PDF file was not generated.");
			}

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
