package views;

import java.io.IOException;
import java.util.HashMap;

public interface Command {
	public void openModal() throws IOException;
	public void closeModal();

	public void openEditModal(HashMap<String, String> data) throws IOException;
	public void closeEditModal();

	public void openDeleteModal(HashMap<String, String> data) throws IOException;
	public void closeDeleteModal();
}
