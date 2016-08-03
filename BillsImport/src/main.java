import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringEscapeUtils;

import com.ryan.jaxb.BillRoot;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class main {
	public static void main(String[] args) {

		/*
		 * Not hitting the api right now. Using local xml bulk data final String
		 * BASE_URI = "https://www.govtrack.us/api/"; final String PATH_BILLS =
		 * "/v2/bill"; ClientConfig config = new DefaultClientConfig(); Client
		 * client = Client.create(config); WebResource resource =
		 * client.resource(BASE_URI);
		 * 
		 * WebResource bills = resource .path(PATH_BILLS)
		 * .queryParam("bill_type__in",
		 * "house_bill|senate_bill|house_joint_resolution|senate_joint_resolution"
		 * ) .queryParam("sort", "-introduced_date") .queryParam("limit", "10");
		 * 
		 * com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
		 * com.google.gson.JsonElement jsonElement = parser
		 * .parse(bills.accept(MediaType.TEXT_XML).get(String.class));
		 */

		// Need to download the zip from:
		// https://www.gpo.gov/fdsys/bulkdata/BILLS/114/1/hr
		// Then unzip
		// Then build map based off file names. Bill number -> List of
		// associated filenames
		// Then loop through and update database with highest status based off
		// file name.

		int congress = 114;
		final String BASE_URI = "https://www.gpo.gov/fdsys/bulkdata/BILLSTATUS/114/hr/BILLSTATUS-114hr999.xml";

		// final String PATH_BILLS = "114/hr/BILLSTATUS-114hr999.xml";
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource resource = client.resource(BASE_URI);

		/*
		 * try { URL url = new URL(BASE_URI); HttpsURLConnection urlConnection =
		 * (HttpsURLConnection) url .openConnection();
		 * 
		 * urlConnection.setRequestMethod("GET");
		 * urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
		 * urlConnection.connect();
		 * 
		 * int response = urlConnection.getResponseCode();
		 * 
		 * File file = new File(
		 * "C:\\Users\\rstap\\Desktop\\workspace\\ImportFromGovTrack\\bills",
		 * "TestingDownload.xml");
		 * 
		 * FileOutputStream fileOutput = new FileOutputStream(file);
		 * 
		 * InputStream inputStream = urlConnection.getInputStream(); byte[]
		 * buffer = new byte[1024];
		 * 
		 * int bufferLength = 0;
		 * 
		 * while ((bufferLength = inputStream.read(buffer)) > 0) { // add the
		 * data in the buffer to the file in the file output // stream (the file
		 * on the sd card fileOutput.write(buffer, 0, bufferLength); }
		 * fileOutput.close();
		 */
		writeToDb();
		/*
		 * } catch (MalformedURLException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

	}

	static void writeToDb() {
		String url = "jdbc:mysql://127.0.0.1:3306/";
		String dbName = "wp807";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "wp807";
		String password = "ScJ5@)PR00";

		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url + dbName,
					userName, password);

			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			Statement st = conn.createStatement();

			Files.walk(
					Paths.get("C:\\Users\\rstap\\Desktop\\workspace\\ImportFromGovTrack\\bills"))
					.forEach(
							filePath -> {
								if (Files.isRegularFile(filePath)) {
									try {
										JAXBContext jaxbContext = JAXBContext
												.newInstance(BillRoot.class);

										Unmarshaller jaxbUnmarshaller = jaxbContext
												.createUnmarshaller();
										BillRoot que = (BillRoot) jaxbUnmarshaller
												.unmarshal(filePath.toFile());

										String id = ""
												+ que.getNumber()
												+ que.getSession()
												+ (que.getType()
														.equalsIgnoreCase("h") ? 1
														: que.getType()
																.equalsIgnoreCase(
																		"hc") ? 2
																: que.getType()
																		.equalsIgnoreCase(
																				"hj") ? 3
																		: que.getType()
																				.equalsIgnoreCase(
																						"hr") ? 4
																				: que.getType()
																						.equalsIgnoreCase(
																								"s") ? 5
																						: que.getType()
																								.equalsIgnoreCase(
																										"sc") ? 6
																								: que.getType()
																										.equalsIgnoreCase(
																												"sj") ? 7
																										: que.getType()
																												.equalsIgnoreCase(
																														"sr") ? 8
																												: 9);

										String statement = "INSERT INTO `wp_posts`("
												+ "`ID`, "
												+ "`post_author`, "
												+ "`post_date`, "
												+ "`post_date_gmt`, "
												+ "`post_content`, "
												+ "`post_title`, "
												+ "`post_excerpt`, "
												+ "`post_status`, "
												+ "`comment_status`, "
												+ "`ping_status`, "
												+ "`post_password`, "
												+ "`post_name`, "
												+ "`to_ping`, "
												+ "`pinged`, "
												+ "`post_modified`, "
												+ "`post_modified_gmt`, "
												+ "`post_content_filtered`, "
												+ "`post_parent`, "
												+ "`guid`, "
												+ "`menu_order`, "
												+ "`post_type`, "
												+ "`post_mime_type`, "
												+ "`comment_count`) "
												+ "VALUES ("
												// / xxxyyzzzz
												+ ""
												+ id
												+ "\n,"
												+ "1"
												+ "\n,'"
												+ que.getIntroduced()
														.getDatetime()
														.toString()
												+ " 00:00:00'"
												+ "\n,'"
												+ que.getIntroduced()
														.getDatetime()
														.toString()
												+ " 00:00:00'"
												+ "\n,'"
												+ StringEscapeUtils
														.escapeEcmaScript(que
																.getTitles()
																.getTitle()
																.get(0)
																.getValue())
												+ "'"
												+ "\n,"
												+ "'"
												+ que.getType().toUpperCase()
												+ "."
												+ que.getNumber()
												+ "'"
												+ "\n,' '"
												+ "\n,'publish'     "
												+ "\n,'open'           "
												+ "\n,'open'        "
												+ "\n,''"
												+ "\n,'"
												+ que.getType().toUpperCase()
												+ "."
												+ que.getNumber()
												+ "'"
												+ "\n,' '"
												+ "\n,' '"
												+ "\n,'"
												+ que.getIntroduced()
														.getDatetime()
														.toString()
												+ " 00:00:00'"
												+ "\n,'"
												+ que.getIntroduced()
														.getDatetime()
														.toString()
												+ " 00:00:00'"
												+ "\n,' '"
												+ "\n,0                       "
												+ "\n,'"
												+ "http://127.0.0.1/wp/?p="
												+ id
												+ "'"
												+ "\n,0"
												+ "\n,'post'"
												+ "\n,' '"
												+ "\n,0)";

										st.executeUpdate(statement);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
