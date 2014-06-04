import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler {
	
	public static void main(String[] args) throws IOException, org.json.simple.parser.ParseException{
		
		//Object obj = parser.parse(new FileReader("c:\\file.json"));

        //JSONObject jsonObject =  (JSONObject) obj;
		String baseUrl = "http://www.justeat.in/bangalore/restaurants";
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("location.json"));
		JSONObject jsonObject = (JSONObject) obj;
		JSONArray jsonArray = (JSONArray) jsonObject.get("data");
		Iterator<JSONObject> iterator = jsonArray.iterator();
		
		ExecutorService executor = Executors.newFixedThreadPool(5);
	
		while(iterator.hasNext()){
			JSONObject placeData = (JSONObject)iterator.next();
			final String url = baseUrl+(String) placeData.get("url");
			final String fileName = ((String) placeData.get("name") +".json").replaceAll(",","-").trim();

			System.out.println(url);

			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					try {
						processPage(url, fileName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//
//					try {
//						Thread.sleep(3000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			});
			
		}
		
	    // This will make the executor accept no new threads
	    // and finish all existing threads in the queue
	    executor.shutdown();
	    // Wait until all threads are finish
	    try {
			executor.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	public static void processPage(String URL, String fileName) throws  IOException{
		
			//get useful information
			
 			//get all links and recursively call the processPage method
//			JsonWriter jsonWriter;
//			jsonWriter = new JsonWriter(new FileWriter("test.json"));
//			jsonWriter.beginObject();
//			jsonWriter.name("data");
//		    jsonWriter.beginArray();
//		    
//		System.setProperty("http.proxyHost", "proxy.wdf.sap.corp");
//		System.setProperty("http.proxyPort", "8080");
			System.getProperties().put("http.proxyHost", "proxy");
			System.getProperties().put("http.proxyPort", "8080");
			Document doc;
			try{
				doc = Jsoup.connect(URL).timeout(5000).get();
			}
			catch( Exception e){
				return;
			}
			
			JSONObject finalObj = new JSONObject();
			JSONArray finalArray = new JSONArray();
			Elements restaurants = doc.select("#SearchResults #OpenRestaurants  article");
			
			for(Element article: restaurants){
			
			String href  = article.select("a.restaurantLogo").get(0).attr("href");
			  //String img = article.select("a.restaurantLogo img").get(0).attr("src");
			  String name = article.select("h3.single-line-truncate").get(0).attr("title");
			  //String address = article.select("address").get(0).text();
			  //System.out.println(name+";;;"+address);
			  //System.out.println(href);
			  JSONObject restaurantObj = new JSONObject();
			  restaurantObj.put("name", name);
			  JSONArray list = processMenu(href);
			  try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  restaurantObj.put("menu", list);
			  finalArray.add(restaurantObj);
			 
				
			}
			finalObj.put("data", finalArray);
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(finalObj.toJSONString());
			fileWriter.flush();
			fileWriter.close();
//			jsonWriter.endArray();
//			jsonWriter.endObject();
//			jsonWriter.close();
		
	}
	public static JSONArray processMenu(String URL) throws IOException {
		JSONArray list = new JSONArray();
		//System.out.println("***********************************************");
		//System.out.println(name);
		//System.out.println("***********************************************");
		Document doc;
		try{
			doc = Jsoup.connect(URL).timeout(5000).get();
		}
		catch( Exception e){
			return list;
		}
		Elements products = doc.select("tbody .prdLi1");
		
//	    jsonWriter.beginObject();
//	    jsonWriter.name("name");
//	    jsonWriter.value(name);
//	    jsonWriter.name("menu");
//	    jsonWriter.beginArray();
		for(Element element: products) {
			if(element.select(".prdDe").size()> 0){
				String prd = element.select(".prdDe").get(0).text();
				String price = element.select(".prdPr").get(0).text();
				System.out.println(prd+"::"+price);
				JSONArray inner = new JSONArray();
				inner.add(prd);
				inner.add(price);
				list.add(inner);
//				jsonWriter.beginArray();
//				jsonWriter.value(prd);
//				jsonWriter.value(price);
//				jsonWriter.endArray();
				
			}
			
			
		}
		return list;
//		jsonWriter.endArray();
//		jsonWriter.endObject();
		
	}
}
