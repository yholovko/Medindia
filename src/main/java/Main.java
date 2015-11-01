import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Main {
    private static Map<String, String> loginCookies;

    private static void login() {
        Connection.Response res = null;
        while (res == null) {
            try {
                res = Jsoup.connect("http://www.medindia.net/regis/login1.asp?Sendto=../dashboard/my-dashboard.asp")
                        .data("txtname", "portexcrawler@gmail.com", "txtpassword", "portexcrawler")
                        .method(Connection.Method.POST)
                        .execute();
                loginCookies = res.cookies();
            } catch (IOException e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static Document connectTo(String url) throws InterruptedException {
        Document doc = null;
        while (doc == null) {
            try {
                doc = Jsoup.connect(url).cookies(loginCookies).timeout(10000).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doc;
    }

    public static void main(String[] args) throws InterruptedException {
        final String GENERICS_URL = "http://www.medindia.net/drug-price/";

        login();

        Document doc = connectTo(GENERICS_URL);

        Set<String> alphabetUrls = new TreeSet<String>();
        Elements elements = doc.select("body > div.container > div.page-content > div > div:nth-child(14) > a");

        for (Element element : elements) {
            alphabetUrls.add(element.attr("href"));
        }

        for (String pages : alphabetUrls) {
            System.out.println(pages);
            doc = connectTo(GENERICS_URL + pages);
            Elements prescribingInformation = doc.select("body > div.container > div.page-content > div > div.vertical-scroll > table > tbody > tr");
            System.out.println("size = " + prescribingInformation.size());
//                for (Element item : prescribingInformation) {
//                    Elements itemElements = item.getAllElements();
//                    System.out.println(itemElements.select("tr > td > a").text());
//                }
        }
    }
}
