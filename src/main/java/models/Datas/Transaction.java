package models.Datas;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.*;
import models.ModelInitializable;
import models.Utils.QueryBuilder;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Transaction implements ModelInitializable {
	private String transactionID         ;
	private String dailySalesHistoryID  ;
	private int soldQuantity        ;
	private String itemID               ;
	private String salesID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.transactionID = data.get("transactionID");
		this.dailySalesHistoryID = data.get("dailySalesHistoryID");
		this.soldQuantity = Integer.parseInt(data.get("soldQuantity"));
		this.itemID = data.get("itemID");
		this.salesID = data.get("salesID");
	}

	// Call this after you load the Item record
	public void enrich(String name, double price) {

	}

	// Plain getters
	public String getTransactionID()        {
		return this.transactionID;
	}
	public String getDailySalesHistoryID()  {
		return this.dailySalesHistoryID;
	}
	public int getSoldQuantity()         {
		return this.soldQuantity;
	}
	public String getItemID()               {
		return this.itemID;
	}
	public String getSalesID()              {
		return this.salesID;
	}

	public String getItemName()
	{
		return itemNameProperty().get();
	}
	public double getUnitPrice()            {
		return unitPriceProperty().get();
	}
	public double getSubtotal()             {
		return this.soldQuantity * this.unitPriceProperty().get();
	}

	// Properties for bindings
	public StringProperty itemNameProperty()            {
		try	{
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			return new SimpleStringProperty(
					qb
						.select(new String[]{"itemName"})
						.from("db/Item.txt")
						.where("itemID", "=", this.itemID)
						.get()
						.get(0)
						.get("itemName"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public DoubleProperty unitPriceProperty()           {
		try	{
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			return new SimpleDoubleProperty(
					Double.parseDouble(
						qb
								.select(new String[]{"unitPrice"})
								.from("db/Item.txt")
								.where("itemID", "=", this.itemID)
								.get()
								.get(0)
								.get("unitPrice"))
					);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public DoubleProperty subtotalProperty()
	{
		return new SimpleDoubleProperty(this.getSubtotal());
	}

	@Override
	public String toString() {
		return "TX#" + getTransactionID()
				+ " item=" + getItemID()
				+ " qty=" + getSoldQuantity()
				+ " sub=" + getSubtotal();
	}

	public IntegerProperty soldQuantityProperty() {
		return new SimpleIntegerProperty(getSoldQuantity());
	}
}
