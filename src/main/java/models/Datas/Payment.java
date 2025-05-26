package models.Datas;

import models.DTO.PaymentDTO;
import models.ModelInitializable;
import models.Utils.FileIO;
import models.Utils.Helper;
import models.Utils.QueryBuilder;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import javax.security.auth.Subject;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	}

	public Payment() {}

	public Payment(String[] data) {
		paymentID = data[0];
		PO_ID = data[1];
		userID = data[2];
		amount = Double.parseDouble(data[3]);
		paymentMethod = data[4];
		createdAt = data[5];
		paymentReference = data[6];
	}

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

	public static String generatePaymentReference(int hashKey) {
		String prefix = "REF";
		String temp = String.valueOf(hashKey);
		String hashCode = Helper.MD5_Hashing(temp).substring(0, 10);

		return MessageFormat.format("{0}{1}", prefix, hashCode);
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

	public static ArrayList<HashMap<String, String>> getPaymentDTO(String PO_ID) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		// List of item ids in this PO
		QueryBuilder<PurchaseOrderItem> qb = new QueryBuilder<>(PurchaseOrderItem.class);
		String[] columns = new String[]{"itemID"};
		ArrayList<HashMap<String, String>> itemIDs = qb.select(columns)
								.from("db/PurchaseOrderItem")
								.where("", "=", PO_ID)
								.get();
		return itemIDs;

//		Set<String> itemIDs = FileIO.filterIDFileBelow("PurchaseOrderItem", )
	}

	public static int getPaymentLatestRowCount() throws IOException {
		Path path = Path.of("src/main/java/db/Payment.txt");
		int lineCount;
		try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
			lineCount = (int) stream.count();
		}
		return lineCount + 1;
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
