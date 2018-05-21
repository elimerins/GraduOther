package crawllerr;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
public class ParseZillowGuide {
	public static void main(String args[]){
		print("running...");
		Document document;
		try {
			//Get Document object after parsing the html from given url.
			document = Jsoup.connect("http://www.zillow.com/denver-co/").get();

			String title = document.title(); //Get title
			print("  Title: " + title); //Print title.
			
			Elements price = document.select(".zsg-photo-card-price:contains($)");
			Elements address = document.select("span[itemprop]:contains(Denver CO)"); //Get address
			for (int i=0; i < price.size(); i++) {
				print(address.get(i).text() + "	" + price.get(i).text());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		print("done");
	}

	public static void print(String string) {
		System.out.println(string);
	}
}
