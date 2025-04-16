# How to use QueryBuilder
## Creating the QueryBuilder instance
You start by creating the QueryBuilder instance with the desired Model to query
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
```

## Retrieving data
You retrieve data by using the `.get()` method or the `.getAsObjects()` method.
The `.get()` method returns an `ArrayList<HashMap<String, String>>`, while the `.getAsObjects()` method returns an `ArrayList<YourModel>`.
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
ArrayList<HashMap<String, String>> res = qb.select().from("db/fileName").get();
ArrayList<YourModel> res = qb.select().from("db/fileName").getAsObjects();
```


## Selecting columns
You select columns by using the `.select()` method. 
This method accepts an array of strings. If no columns are selected, the default column is selected which is `*`.
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
String[] columns = new String[]{"name", "age", "id"};
ArrayList<HashMap<String, String>> res = qb.select(columns).from("db/fileName").get();
```

## Targeting files (read or write)
When reading files, you target the file you want to read using `.from()`method.
The `.from()`method reads from `src/main/java/` by default.
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
ArrayList<HashMap<String, String>> res = qb.select().from("db/fileName").get();
```

When writing files, you target the file you want to write to using `.target()`method.
The `.target()`method starts from `src/main/java/` by default.
```java
qb.target("db/fileName").values(new String[]{"Allen", "21", "1"}).create();
```

## Where clause
You can use the `.where()` method to add a where clause to the query.
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
ArrayList<HashMap<String, String>> res = qb.select().from("db/fileName").where("id", "=", "1").get();
```
Additionally, you can add logical operators such as `.and()` and `.or()` to the where clause.
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
ArrayList<HashMap<String, String>> res = qb.select().from("db/fileName").where("id", "=", "1").and("name", "=", "John").get();
```

## Sort by clause
You can use the `.sort()` method to sort the data.
By default, the data is sorted by the first column in ascending order.
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
ArrayList<HashMap<String, String>> res = qb.select().from("db/fileName").sort("id", "desc").get();
```

## Creating new entries
You can use the `.create()` method to create a new entry in the database.
The values are passed as an array of strings.
**Ensure that the order of strings are the same as the order of columns.**
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
qb.target("db/fileName").values(new String[]{"Allen", "21", "1"}).create();
```

## Updating entries
You can use the `.update()` method to update an entry in the database.
The `update()`method takes the target ID and the values to be updated.
The values are passed as an array of strings.
**Ensure that the order of strings are the same as the order of columns.**
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
qb.update("9", new String[]{"Doe", "22", "1"});
```

There are other updates methods such as `.updateMany()`, `.updateManyParallelMap()`, and `.updateManyParallelArr()`.
`.updateMany()` updates multiple IDs inside the database with the same data inputted.
`.updateManyParallelMap()` and `.updateManyParallelArr()` updates multiple IDs inside the database with the multiple data inputted.
**Number of targets must be equal to number of data inputted.**

`updateManyParallelMap()`should be used to update many **specific parts** of the data inside the database.
`updateManyParallelArr()`should be used to update many data of different content inside the database.

```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);

ArrayList<HashMap<String, String>> someDataToUpdate = new ArrayList<>();
ArrayList<String[]> someDataToUpdateArr = new ArrayList();
HashMap<String, String> dataToUpdate = new HashMap<>();

someDataToUpdateArr.add(new String[]{"Doe", "22", "1"});
someDataToUpdateArr.add(new String[]{"Cook", "24", "3"});

dataToUpdate.put("name", "John");
dataToUpdate.put("age", "22");
dataToUpdate.put("id", "1");
someDataToUpdate.add(dataToUpdate);

qb.updateMany(new String[] {"9", "10"},new String[]{"Doe","22","1"});
qb.updateManyParallelMap(new String[] {"9", "10"}, someDataToUpdate);
qb.updateManyParallelArr(new String[] {"9", "10"}, someDataToUpdateArr);
```

## Deleting entries
You can use the `.delete()` method to delete an entry in the database.
The `delete()`method takes the target ID.
```java
QueryBuilder<YourModel> qb = new QueryBuilder(YourModel.class);
qb.target("db/fileName").delete("9");
```