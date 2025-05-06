package models.Datas;

import models.ModelInitializable;
import models.Utils.Helper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import javax.security.auth.Subject;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;

public class Payment implements ModelInitializable {

	public enum PaymentMethod {
		Bank, TnG
	};

	private String paymentID;
	private String PO_ID;
	private String userID;
	private double amount;
	private String paymentMethod;
	private String createdAt;
	private String paymentReference;

	public Payment(String method, double amount, String PO_ID, String userID) {
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

	public Payment() {}

	@Override
	public void initialize(HashMap<String, String> data) {
		paymentID = data.get("paymentID") != null ? data.get("paymentID") : "";
		PO_ID = data.get("PO_ID");
		userID = data.get("userID");
		amount = Double.parseDouble(data.get("amount"));
		paymentMethod = data.get("paymentMethod");
		createdAt = data.get("createdAt");
		paymentReference = data.get("paymentReference");
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

	public String getPaymentMethod() {
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

	public String[] convertToArr() throws IllegalAccessException {
		Field[] fields = Subject.class.getDeclaredFields();
		ArrayList<String> result = new ArrayList<>();

		for (Field field : fields) {
			field.setAccessible(true);
			Object val = field.get(this);
			result.add(String.valueOf(val));
		}

		return result.toArray(new String[0]);
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
