package service;

import models.DTO.ItemListDTO;
import models.Datas.InventoryUpdateRequest;
import models.Datas.Item;
import models.Datas.Supplier;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemService extends Service<ItemListDTO> {
	@Override
	public List<ItemListDTO> getAll() {
		List<ItemListDTO> list = new ArrayList<>();
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			ArrayList<HashMap<String, String>> items = qb
					.select()
					.from("db/Item")
					.joins(Supplier.class, "supplierID")
					.get();
			for (HashMap<String, String> item : items) {
				ItemListDTO dto = new ItemListDTO(
				item.get("itemID"),
				item.get("itemName"),
				item.get("description"),
				Item.formatDateTime(item.get("createdAt")),
				Item.formatDateTime(item.get("updatedAt")),
				Integer.parseInt(item.get("alertSetting")),
				Integer.parseInt(item.get("quantity")),
				Double.parseDouble(item.get("unitPrice")),
				item.get("supplierID"),
				item.get("supplierName")
				);
				list.add(dto);
			}
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean add(String... varargs) {
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			QueryBuilder<InventoryUpdateRequest> qb2 = new QueryBuilder<>(InventoryUpdateRequest.class);
			boolean resItem = qb
					.target("db/Item")
					.values(new String[]{
							varargs[0],
							varargs[1],
							LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
							LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
							"10",
							varargs[2],
							varargs[3],
							varargs[4],
					})
					.create();

			if (!resItem) return false;

			List<Item> latestItems = qb
					.select()
					.from("db/Item")
					.joins(Supplier.class, "supplierID")
					.getAsObjects();
			Item latestItem = latestItems.get(latestItems.size() - 1);
            return qb2
                    .target("db/InventoryUpdateRequest")
                    .values(new String[]{
                            latestItem.getItemID(),
                            SessionManager.getInstance().getUserData().get("userID"),
                            "0",
                            "Pending"
                    })
                    .create("REQ");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean update(String itemID, HashMap<String, String> dataToUpdate) {
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			return qb
					.target("db/Item")
					.update(itemID, dataToUpdate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean delete(String itemID) {
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			return qb
					.target("db/Item")
					.delete(itemID);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}