package models.Utils;

import models.ModelInitializable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class used to query from a target db file.
 * The root path the class uses is ./src
 * <br>
 * Example usage:
 * <pre>
 * <code>
 * QueryBuilder&lt;SomeClass&gt; qb = new QueryBuilder&lt;&gt;(new SomeClass());<br>
 * qb.select("name", "age").from("db/fileName").where("name", "=", "John").get();<br>
 * qb.target("db/fileName").values(new String[]{"Allen", "21", "1"}).create();<br>
 * qb.update("9", new String[]{"Doe", "22", "1"});<br>
 * qb.target("db/fileName").delete("9");
 * </code>
 * </pre>
 *
 * @param <T> The type of the class to be used.
 * */
public class QueryBuilder<T extends ModelInitializable>{
	private final String FILE_ROOT = "src/main/java/";

	private final T aClass;
	private final Class<T> aClassType;
	private final String[] classAttrs;

	private String fileName;
	private String[] selectedColumns;
	private String[] whereClause;
	private final ArrayDeque<String[]> andOperatorStack;
	private final ArrayDeque<String[]> orOperatorStack;
	private final ArrayDeque<Integer> queue;
	private String[] sortByClause;

	private String targetFile;
	private String createValues;

	private ArrayList<Class<? extends ModelInitializable>> joins;
	private ArrayList<String> joinKey;

	public QueryBuilder(Class<T> someClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		this.aClassType = someClass;
		this.aClass = someClass.getDeclaredConstructor().newInstance();
		this.classAttrs = getAttrs();
		this.andOperatorStack = new ArrayDeque<>();
		this.queue = new ArrayDeque<>();
		this.orOperatorStack = new ArrayDeque<>();
		this.joins = new ArrayList<>();
		this.joinKey = new ArrayList<>();
		String className = getClassName();
		setClassName(className);
	}

	public String getClassName() {
		return aClass.getClass().getSimpleName();
	}

	public void setClassName(String className){
		this.fileName = className;
	}

	/**
	 * Returns an QueryBuilder of the type you passed in.
	 * The columns are <b>optional</b>.
	 * The default column is <b>" * "</b>.
	 *
	 * @param columns <b>Optional: String[]</b> <br> An optional array of strings.
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 *
	 * */
	public QueryBuilder<T> select(String[]... columns){
		if (columns.length == 0) {
			this.selectedColumns = new String[]{"all"};
		}else {
			this.selectedColumns = columns[0];
		}
		return this;
	}

	/**
	 * Returns an QueryBuilder of the type you passed in.
	 * The root path is /src.
	 * The fileName argument must be of type <b>String</b>, and can omit the
	 * .txt
	 * <p>
	 *
	 * @param  fileName <b>String</b> <br> The path of the file relative to ./src without the file extension
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 **/
	public QueryBuilder<T> from(String fileName) {
		if (fileName.contains(".txt")) {
			fileName = fileName.replace(".txt", "");
		}
		this.fileName = fileName;
		return this;
	}

	/**
	 * Sets the where clause to be used while querying data.
	 *
	 * @param fieldOne <b>String</b> <br> The left side of the comparison.
	 * @param operator <b>String</b> <br> The comparison operator. Currently supported operators are:
	 *                 '=', '!=', 'like'
	 *
	 * @param fieldTwo <b>String</b> <br> The right side of the comparison.
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 * */
	public QueryBuilder<T> where(String fieldOne, String operator, String fieldTwo) {
		this.whereClause = new String[]{fieldOne, operator, fieldTwo};
		return this;
	}

	public QueryBuilder<T> and(String fieldOne, String operator, String fieldTwo) {
		if (this.whereClause == null) {
			throw new RuntimeException("No where clause has been set");
		}
		this.andOperatorStack.add(new String[]{fieldOne, operator, fieldTwo});
		this.queue.add(1);
		return this;
	}

	public QueryBuilder<T> or(String fieldOne, String operator, String fieldTwo) {
		if (this.whereClause == null) {
			throw new RuntimeException("No where clause has been set");
		}
		this.orOperatorStack.add(new String[]{fieldOne, operator, fieldTwo});
		this.queue.add(0);
		return this;
	}

	/**
	 * Sets the sort by clause to be used when querying data.
	 * Defaults to sorting by the ID field.
	 *
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 * */
	public QueryBuilder<T> sort() {
		this.sortByClause = new String[]{getClassName().toLowerCase() + "_id", "asc"};
		return this;
	}

	/**
	 * Sets the joins to be used when querying data.
	 *
	 * @param joins <b>Class</b> <br> The class to be joined.
	 * @param joinKey <b>String</b> <br> The join key.
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 * */
	public QueryBuilder<T> joins(Class<? extends ModelInitializable> joins, String joinKey) {
		this.joins.add(joins);
		this.joinKey.add(joinKey);
		return this;
	}

	/**
	 * Sets the sort by clause to be used when querying data.
	 * Defaults to sorting by the ID field.
	 *
	 * @param itemToSortBy <b>String</b> <br> The item to be sorted by.
	 * @param order <b>String</b> <br> The order to be sorted by. Currently supported orders are:
	 *              <b>asc</b>, <b>desc</b>
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 * */
	public QueryBuilder<T> sort(String itemToSortBy, String order) {
		this.sortByClause = new String[]{itemToSortBy, order};
		return this;
	}

	/**
	 * Retrieves the data according to the statements that have been used.
	 * Retrieves the data from the path ./src/main/java/{fileName}.txt
	 * Use the <b>customFrom()</b> method to change this.
	 *
	 * @return An array with HashMaps with the instances of the class passed into the QueryBuilder.
	 * */

	public ArrayList<T> getAsObjects() {
		ArrayList<HashMap<String, String>> data = this.get();
		ArrayList<T> objects = new ArrayList<>();

		for (HashMap<String, String> item: data) {
			try {
				T instance = this.aClassType.getDeclaredConstructor().newInstance();
				instance.initialize(item);
				objects.add(instance);
			} catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
			         IllegalAccessException e) {
				System.out.println(e.getMessage());
			}
		}

		return objects;
	}

	/**
	 * Retrieves the data according to the statements that have been used.
	 * Retrieves the data from the path ./src/main/java/{fileName}.txt
	 * Use the <b>customFrom()</b> method to change this.
	 *
	 * @return An array with HashMaps with the columns as the key and the data as the item.
	 * */
	public ArrayList<HashMap<String, String>> get(){
		ArrayList<HashMap<String, String>> allData = new ArrayList<>();
		String textFileName = this.fileName + ".txt";

		try {
			BufferedReader br = new BufferedReader(new FileReader(FILE_ROOT + textFileName));
			String line = br.readLine();

			// Where filtering
			while (line != null) {
				ArrayList<HashMap<String, String>> dataHolder = getHashMaps(line.split(","), this.classAttrs);
				allData.addAll(dataHolder);
				line = br.readLine();
			}

			//Joins
			if (!this.joins.isEmpty()) {
				for (Class<? extends ModelInitializable> join: this.joins) {
					String joinName = join.getSimpleName().toLowerCase();
					String joinTextFileName = joinName + ".txt";

					if (allData.get(0).get(joinName+"_id") == null) {
						throw new RuntimeException("No " + joinName + " ID found");
					}

					try {
						//Create the join query builder
						QueryBuilder<?> joinQb = new QueryBuilder<>(join);
						ArrayList<HashMap<String, String>> joinData = joinQb
								.select()
								.from("db/" + joinTextFileName)
								.get();

						// Key to join by
						String key = this.joinKey.get(this.joins.indexOf(join));
						// Lookup map
						Map<String, HashMap<String, String>> modelLookup = new HashMap<>();
						for (HashMap<String, String> model : joinData) {
							if (model.containsKey(key)) {
								modelLookup.put(model.get(key), model);
							}
						}

						// Merging the data
						allData = allData.stream().map(item -> {
							String valueToMerge = item.get(key);
							HashMap<String, String> mergedItem = new HashMap<>(item);
							Set<String> fieldsToMerge = new HashSet<>(List.of(joinQb.getAttrs(true)));

							Optional.ofNullable(modelLookup.get(valueToMerge))
									.ifPresent(matchingModel ->
										fieldsToMerge.stream()
											.filter(matchingModel::containsKey)
											.forEach(field ->
												mergedItem.put(field, matchingModel.get(field))
											)
									);

							return mergedItem;
						}).collect(Collectors.toCollection(ArrayList::new));

					}catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}

			//Sort By Statement
			if (this.sortByClause != null) {
				switch (this.sortByClause[1]) {
					case "asc":
						if (allData.get(0).get(this.sortByClause[0]).matches("[0-9]+")) {
							allData.sort(Comparator.comparingInt(o -> Integer.parseInt(o.get(this.sortByClause[0]))));
						} else {
							allData.sort(Comparator.comparing(o -> o.get(this.sortByClause[0])));
						}
						break;
					case "desc":
						//TODO reverse sort for integers
						if (allData.get(0).get(this.sortByClause[0]).matches("[0-9]+")) {
							allData.sort(Comparator.comparingInt(o -> Integer.parseInt(o.get(this.sortByClause[0]))));
						} else {
							allData.sort((o1, o2) -> o2.get(this.sortByClause[0]).compareTo(o1.get(this.sortByClause[0])));
						}
						break;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return allData;
	}

	/**
	 *
	 * Runs the switch case for the where clause
	 *
	 * @param equation The equation to be run
	 * @param dataHolder The data holder with the data to be filtered
	 */
	private void updateSwitchCase(String[] equation, ArrayList<HashMap<String, String>> dataHolder) {
		if (equation[2].matches("[0-9]+")) {
			int value = Integer.parseInt(equation[2]);
			switch (equation[1]) {
				case "=":
					dataHolder.removeIf(data -> !(Integer.parseInt(data.get(equation[0])) == value));
					break;
				case "!=":
					dataHolder.removeIf(data -> Integer.parseInt(data.get(equation[0])) == value);
					break;
				case ">":
					dataHolder.removeIf(data -> !(Integer.parseInt(data.get(equation[0])) > value));
					break;
				case "<":
					dataHolder.removeIf(data -> !(Integer.parseInt(data.get(equation[0])) < value));
					break;
				case ">=":
					dataHolder.removeIf(data -> !(Integer.parseInt(data.get(equation[0])) >= value));
					break;
				case "<=":
					dataHolder.removeIf(data -> !(Integer.parseInt(data.get(equation[0])) <= value));
					break;
			}
		} else {
			switch (equation[1]) {
				case "=":
					dataHolder.removeIf(data-> !data.get(equation[0]).equals(equation[2]));
					break;
				case "!=" :
					dataHolder.removeIf(data-> data.get(equation[0]).equals(equation[2]));
					break;
				case "like":
					dataHolder.removeIf(data-> !data.get(equation[0]).contains(equation[2]));
					break;
			}

		}

	}

	/**
	 *
	 * Checks if the data to be added is already in the data holder stack
	 *
	 * @param dataHolderStack The data holder stack to compare to
	 * @param dataToAdd The data that is to be added if not in dataHolderStack
	 * @return boolean
	 */
	private boolean checkDuplicates(ArrayDeque<ArrayList<HashMap<String, String>>> dataHolderStack, ArrayList<HashMap<String, String>> dataToAdd) {
		for (ArrayList<HashMap<String, String>> list : dataHolderStack) {
			if (list.size() == dataToAdd.size() && list.containsAll(dataToAdd) && dataToAdd.containsAll(list)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * Recursively parses the queue of the logical operators
	 *
	 * @param queue The queue of the logical operators
	 * @param dataHolder The data holder
	 * @param dataHolderStack The data holder stack
	 * @param dataCopy The copy of the data holder
	 * @param andStack The stack of the AND statements
	 * @param orStack The stack of the OR statements
	 * @return The data holder stack
	 */
	private ArrayDeque<ArrayList<HashMap<String, String>>> recursiveLogicalOperatorCheck(ArrayDeque<Integer> queue, ArrayList<HashMap<String, String>> dataHolder, ArrayDeque<ArrayList<HashMap<String, String>>> dataHolderStack, ArrayList<HashMap<String, String>> dataCopy, ArrayDeque<String[]> andStack, ArrayDeque<String[]> orStack) {
		Iterator<Integer> iterator = queue.iterator();
		ArrayDeque<String[]> andStackClone = andStack.clone();
		if (queue.isEmpty()) {
			return dataHolderStack;
		}
		//If only item is an OR statement
		if (queue.size() == 1 && queue.peek() == 0) {
			updateSwitchCase(orStack.getFirst(), dataCopy);

			if (!dataCopy.isEmpty()) {
				dataHolderStack.add(dataCopy);
			}

			return dataHolderStack;
		} else {
			while (iterator.hasNext()) {
				int index = iterator.next();
				//If the queue has an OR statement, but there are still AND statements in the stack.
				//Example: queue: [OR, AND], AND stack: id = 1, OR stack: id = 5
				if (index == 0 && !andStackClone.isEmpty()) {
					//Reverse the queue
					ArrayDeque<Integer> reverseQueue = new ArrayDeque<>();
					for (int i: queue) {
						reverseQueue.push(i);
					}

					return recursiveLogicalOperatorCheck(reverseQueue, dataCopy, dataHolderStack, dataCopy, andStackClone, orStack);
				}//If the queue has an OR statement and the AND stack is empty
				else if (index == 0) {
					updateSwitchCase(orStack.getFirst(), dataHolder);

					if (!dataHolder.isEmpty()) {
						dataHolderStack.add(dataHolder);
					}

					iterator.remove();
				}//If current item in queue is AND statement
				else {
					updateSwitchCase(andStackClone.getFirst(), dataHolder);

					if (!dataHolder.isEmpty()) {
						if (!checkDuplicates(dataHolderStack, dataHolder)) {
							dataHolderStack.add(dataHolder);
						}
					}

					iterator.remove();
					andStackClone.remove();
				}
			}
		}
		return dataHolderStack;
	}

	/**
	 * Gets the Hashmap which contains fields selected using the select() method.
	 *
	 * @param entryData <b>String[]</b> <br> An array that contains the selected fields from the user.
	 * @param classAttrs <b>String[]</b> <br> The attributes of the class passed into QueryBuilder.
	 * @return ArrayList that contains the hashmap of the selected fields.
	 *
	 * */
	private ArrayList<HashMap<String, String>> getHashMaps(String[] entryData, String[] classAttrs) {
		HashMap<String, String> dataMap = new HashMap<>();
		ArrayList<HashMap<String, String>> dataHolder = new ArrayList<>();
		ArrayList<HashMap<String, String>> dataCopy = new ArrayList<>();
		ArrayDeque<ArrayList<HashMap<String, String>>> dataHolderStack = new ArrayDeque<>();

		for (int i = 0; i < classAttrs.length; i++) {
			dataMap.put(classAttrs[i], entryData[i]);
		}

		dataHolder.add(dataMap);
		dataCopy.add(dataMap);

		//Where Statement
		if (this.whereClause != null) {
			updateSwitchCase(this.whereClause, dataHolder);
			if (!dataHolder.isEmpty()){
				dataHolderStack.add(dataHolder);
			}
		}

		if (!this.queue.isEmpty()) {
			ArrayDeque<Integer> queueCopy = this.queue.clone();
			recursiveLogicalOperatorCheck(queueCopy, dataHolder, dataHolderStack, dataCopy, this.andOperatorStack, this.orOperatorStack);
		}

		if (!dataHolderStack.isEmpty()) {
			ArrayList<HashMap<String, String>> temp = new ArrayList<>();
			for (int i = 0; i < dataHolderStack.size(); i++) {
				if (!dataHolderStack.peek().isEmpty()) {
					HashMap<String, String> item = dataHolderStack.pop().get(0);
					temp.add(item);
				}
			}
			dataHolder = temp;
			temp = null;
			System.gc();
		}

		//Select Statement
		if (this.selectedColumns[0].equals("all")) {
			return dataHolder;
		}else {
			for (String classAttr : classAttrs) {
				if (!Arrays.asList(this.selectedColumns).contains(classAttr)) {
					dataHolder.forEach(data-> data.remove(classAttr));
				}
			}
		}
		return dataHolder;
	}

	/**
	 * Sets target file to be modified.
	 *
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 * */
	public QueryBuilder<T> target(){
		this.targetFile = getClassName();
		return this;
	}

	/**
	 * Sets target file to be modified.
	 *
	 * @param target <b>String</b> <br> The path of the file relative to ./src without the file extension
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 * */
	public QueryBuilder<T> target(String target){
		if (target.contains(".txt")) {
			target = target.replace(".txt", "");
		}
		this.targetFile = target;
		return this;
	}

	/**
	 * Sets the values that will be added into the target file
	 *
	 * @param dataArr <b>String[]</b> <br> The values that will be added into the target file.
	 * @return a QueryBuilder of the type you passed in for method chaining.
	 * */
	public QueryBuilder<T> values(String[] dataArr){
		this.createValues = String.join(",", dataArr);
		return this;
	}

	/**
	 * Inserts the new data into the target file.
	 *
	 * @see QueryBuilder#validateData(String values)
	 * @see QueryBuilder#validateData(String values, Boolean isTargeted)
	 * @see QueryBuilder#target()
	 * @see QueryBuilder#values(String[] dataArr)
	 * @throws IOException will throw error if file does not exist or validation fails
	 * */
	public boolean create() throws IOException {
		HashMap<String, String> validatedData = this.validateData(this.createValues);
		FileWriter fw = new FileWriter(FILE_ROOT + this.targetFile + ".txt", true);
		ArrayList<HashMap<String, String>> data = this.select(new String[]{this.getClassName().toLowerCase()+"_id"})
				.from(this.targetFile)
				.get();
		int latestId = Integer.parseInt(data.get(data.size()-1)
				.get(this.getClassName().toLowerCase()+"_id"))+1;
		try {
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder lineToWrite = new StringBuilder(latestId + ",");
			for (String item: this.getAttrs(false)) {
				lineToWrite.append(validatedData.get(item)).append(",");
			}
			//Remove last comma
			bw.write(lineToWrite.substring(0, lineToWrite.length()-1));
			bw.newLine();
			bw.close();
			fw.close();
			return true;
		} catch (IOException e) {
			return false;
//			throw new RuntimeException(e);
		}
	}

	/**
	 * Updates a specific part of an item inside the database.
	 * <b>.target()</b> should be used to set the target file.
	 *
	 * @throws IOException will throw error if file does not exist
	 * @param targetId <b>String</b> <br> The id of the data that will be updated.
	 * @param targetChange <b>HashMap</b> <br> The data that will be updated.
	 * @see QueryBuilder#target()
	 * */
	public boolean update(String targetId, HashMap<String, String> targetChange) throws IOException {
		HashMap<String, String> validatedData = this.validateData(String.join(",", targetChange.values()), true);
		String targetFile = (this.targetFile != null ? this.targetFile : "db/" +this.getClassName().toLowerCase()) + ".txt";

		FileReader fr = new FileReader(FILE_ROOT + targetFile);
		try {
			BufferedReader br = new BufferedReader(fr);

			ArrayList<String> lines = new ArrayList<>();
			ArrayList<String> dataToWrite = new ArrayList<>();

			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			FileWriter fw = new FileWriter(FILE_ROOT + targetFile, false);
			BufferedWriter bw = new BufferedWriter(fw);
			String[] classAttrs = this.getAttrs();

			for (String s : lines) {
				HashMap<String, String> lineData = new HashMap<>();

				for (int j = 0; j < classAttrs.length; j++) {
					lineData.put(classAttrs[j], s.split(",")[j]);
				}

				if (s.split(",")[0].equals(targetId)) {
					for (String attr : classAttrs) {
						if (attr.equals(targetChange.keySet().toArray()[0])) {
							dataToWrite.add(targetChange.get(attr));
						} else {
							dataToWrite.add(lineData.get(attr));
						}
					}
					bw.write(String.join(",", dataToWrite));
					bw.newLine();
				} else {
					bw.write(s);
					bw.newLine();
				}
			}
			bw.close();
			fw.close();

			return true;
		} catch (IOException e) {
			return false;
//			throw new RuntimeException(e);
		}
	}

	/**
	 * Updates all parts of a specific item inside the database.
	 *
	 * @throws IOException will throw error if file does not exist
	 * @param targetId <b>String</b> <br> The id of the data that will be updated.
	 * @param data <b>String[]</b> <br> The data that will be updated.
	 * @see QueryBuilder#target()
	 * */
	public boolean update(String targetId, String[] data) throws IOException {
		HashMap<String, String> validatedData = this.validateData(String.join(",", data));
		String targetFile = (this.targetFile != null ? this.targetFile : "db/" +this.getClassName().toLowerCase()) + ".txt";

		FileReader fr = new FileReader(FILE_ROOT + targetFile);
		try {
			BufferedReader br = new BufferedReader(fr);

			ArrayList<String> lines = new ArrayList<>();
			ArrayList<String> dataToWrite = new ArrayList<>();

			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			FileWriter fw = new FileWriter(FILE_ROOT + targetFile, false);
			BufferedWriter bw = new BufferedWriter(fw);
			String[] classAttrs = this.getAttrs(false);

			for (String lineItem: lines) {
				if (lineItem.split(",")[0].equals(targetId)) {
					dataToWrite.add(targetId);
					for (String attr: classAttrs) {
						dataToWrite.add(validatedData.get(attr));
					}
					bw.write(String.join(",", dataToWrite));
					bw.newLine();
				}else {
					bw.write(lineItem);
					bw.newLine();
				}
			}
			bw.close();
			fw.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Updates multiple IDs inside the database with the same data inputted.
	 *
	 * @param data <b>String[]</b> <br> The data that will be updated.
	 * @param targetIds <b>String[]</b> <br> The ids of the data that will be updated.
	 * @throws IOException will throw error if file does not exist
	 * */
	public boolean updateMany(String[] targetIds, String[] data) throws IOException {
		for (String id : targetIds) {
			boolean res = this.update(id, data);
			if (!res) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates multiple IDs inside the database with the same data inputted.
	 *
	 * @param targetChange <b>HashMap&lt;String, String&gt;</b> <br> The data that will be updated.
	 * @param targetIds <b>String[]</b> <br> The ids of the data that will be updated.
	 * @throws IOException will throw error if file does not exist
	 * */
	public boolean updateMany(String[] targetIds, HashMap<String, String> targetChange) throws IOException {
		for (String id : targetIds) {
			boolean res = this.update(id, targetChange);
			if (!res) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates multiple IDs inside the database with the multiple data inputted.
	 * Number of targets must be equal to number of data
	 *
	 * @param targetChanges <b>ArrayList&lt;HashMap&lt;String, String&gt;&gt;</b> <br> The data that will be updated.
	 * @param targetIds <b>String[]</b> <br> The ids of the data that will be updated.
	 * @throws IOException will throw error if file does not exist
	 * */
	public boolean updateManyParallelMap(String[] targetIds, ArrayList<HashMap<String, String>> targetChanges) throws IOException {
		if (targetIds.length != targetChanges.size()) {
			throw new RuntimeException("Number of targets must be equal to number of data");
		}
		for (int i = 0; i < targetIds.length; i++) {
			boolean res = this.update(targetIds[i], targetChanges.get(i));
			if (!res) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates multiple IDs inside the database with multiple data inputted.
	 * Number of targets must be equal to number of data
	 *
	 * @param data <b>ArrayList&lt;String[]&gt;</b> <br> The data that will be updated.
	 * @param targetIds <b>String[]</b> <br> The ids of the data that will be updated.
	 * @throws IOException will throw error if file does not exist
	 * */
	public boolean updateManyParallelArr(String[] targetIds, ArrayList<String[]> data) throws IOException {
		if (targetIds.length != data.size()) {
			throw new RuntimeException("Number of targets must be equal to number of data");
		}
		for (int i = 0; i < targetIds.length; i++) {
			boolean res = this.update(targetIds[i], data.get(i));
			if (!res) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Deletes a specific item inside the database.
	 *
	 * @param targetId <b>String</b> <br> The id of the data that will be deleted.
	 * @throws FileNotFoundException will throw error if file does not exist
	 * @see QueryBuilder#target()
	 * */
	public boolean delete(String targetId) throws FileNotFoundException {
		String targetFile = (this.targetFile != null ? this.targetFile : this.getClassName().toLowerCase()) + ".txt";

		FileReader fr = new FileReader(FILE_ROOT + targetFile);
		try {
			BufferedReader br = new BufferedReader(fr);

			ArrayList<String> lines = new ArrayList<>();

			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			FileWriter fw = new FileWriter(FILE_ROOT + targetFile, false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (String lineItem: lines) {
				if (!lineItem.split(",")[0].equals(targetId)) {
					bw.write(String.join(",", lineItem));
					bw.newLine();
				}
			}
			bw.close();
			fw.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Validates the data entered by the user.
	 *
	 * @param values <b>String</b> <br> The values that will be validated.
	 * @throws RuntimeException will throw error if validation fails.
	 * @return HashMap that contains the validated data.
	 * */
	public HashMap<String, String> validateData(String values){
		HashMap<String, String> dataMap = new HashMap<>();

		String[] classAttrs = this.getAttrs(false);
		String[] dataToValidate = values.split(",");

		if (dataToValidate.length != classAttrs.length) {
			throw new RuntimeException("Number of values does not match number of attributes");
		}

		for (int i = 0; i < classAttrs.length; i++) {
			dataMap.put(classAttrs[i], dataToValidate[i]);
		}

		for (String item: dataMap.keySet()) {
			if (dataMap.get(item).equals("null")) {
				throw new RuntimeException("Null values are not allowed");
			}
		}

		if (dataMap.get("age") != null &&!dataMap.get("age").matches("-?\\d+(\\\\.\\d+)?")){
			throw new RuntimeException("Age must be a number");
		}

		return dataMap;
	}

	/**
	 * Validates specific data that is entered by the user.
	 *
	 * @param values <b>String</b> <br> The data that will be validated.
	 * @param isTargeted <b>Boolean</b> <br> If the data is being targeted.
	 * @throws RuntimeException will throw error if validation fails.
	 * @return HashMap that contains the validated data.
	 * */
	public HashMap<String, String> validateData(String values, Boolean isTargeted){
		HashMap<String, String> dataMap = new HashMap<>();

		String[] classAttrs = this.getAttrs(false);
		String[] dataToValidate = values.split(",");

		if (dataToValidate.length != classAttrs.length && !isTargeted) {
			throw new RuntimeException("Number of values does not match number of attributes");
		}

		for (int i = 0; i < dataToValidate.length; i++) {
			dataMap.put(classAttrs[i], dataToValidate[i]);
		}

		for (String item: dataMap.keySet()) {
			if (dataMap.get(item).equals("null")) {
				throw new RuntimeException("Null values are not allowed");
			}
		}

		if (dataMap.get("age") != null && !dataMap.get("age").matches("-?\\d+(\\\\.\\d+)?")){
			throw new RuntimeException("Age must be a number");
		}

		return dataMap;
	}

	/**
	 * Adds an element to the start of an array.
	 * Used for adding the id of the class into the available attributes.
	 *
	 * @param array <b>T[]</b> <br> The array to be modified.
	 * @param element <b>T</b> <br> The item to be added into the array.
	 * @return The new array with the element at index 0.
	 * */
	public static <T> T[] addToBeginningOfArray(T[] array, T element) {
		T[] result = Arrays.copyOf(array, array.length + 1);
		result[0] = element;
		System.arraycopy(array, 0, result, 1, array.length);

		return result;
	}

	/**
	 * Returns an array of <b>String</b> containing the attributes of the class
	 * passed into the <b>QueryBuilder</b> class.
	 *
	 * @return An array of <b>String</b> with the class fields.
	 * */
	public String[] getAttrs(){
		Field[] attrs = this.aClass.getClass().getDeclaredFields();

		return Arrays.stream(attrs).map(Field::getName).toArray(String[]::new);
	}

	/**
	 * Returns an array of <b>String</b> containing the attributes of the class
	 * passed into the <b>QueryBuilder</b> class.
	 *
	 * @param withId <b>Boolean</b> <br> If true, the id of the class will be included
	 * @return An array of <b>String</b> with the class fields.
	 * */
	public String[] getAttrs(Boolean withId){
		Field[] attrs = this.aClass.getClass().getDeclaredFields();

		int n = Arrays.stream(attrs).map(Field::getName).toArray(String[]::new).length-1;
		String[] classAttrs = new String[n];
		System.arraycopy(Arrays.stream(attrs).map(Field::getName).toArray(String[]::new),1,classAttrs,0, n);

		return withId ?
				Arrays.stream(attrs).map(Field::getName).toArray(String[]::new) :
				classAttrs;
	}

}