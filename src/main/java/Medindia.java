import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Medindia {
    private Map<String, String> loginCookies = new LinkedHashMap<String, String>();

    public Medindia() {
        loginCookies.put("ASPSESSIONIDQCSCRDDT", "LKCCNEADMGGFJJIIJHDHAPCE");
        System.setProperty("http.proxyHost", "190.98.162.22");
        System.setProperty("http.proxyPort", "8080");
    }

    public Document connectTo(String url) throws InterruptedException {
        Document doc = null;
        while (doc == null) {
            try {
                doc = Jsoup.connect(url).cookies(loginCookies).timeout(30000).get();
                if (doc.text().contains("Please note copying of data is not allowed from Medindia") || doc.text().contains("Captcha | Medindia")){
                    doc = null;
                    throw new IOException("Blocked");
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return doc;
    }

    public void getGenericList() throws InterruptedException {
        Document doc = connectTo(Constants.DRUGS_URL);

        Set<String> alphabetUrls = new TreeSet<>();
        Elements elements = doc.select("body > div.container > div.page-content > div > div:nth-child(14) > a");

        for (Element element : elements) {
            alphabetUrls.add(element.attr("href")); //get all links (A, B, C ... Z)
        }

        for (String pages : alphabetUrls) {
            doc = connectTo(Constants.DRUGS_URL + pages);
            Elements drugs = doc.select("body > div.container > div.page-content > div > div.vertical-scroll > table > tbody > tr > td:nth-child(2) > a");
                for (Element drug : drugs) {
                    doc = connectTo(drug.attr("href"));

                    DrugInfo drugInfo = new DrugInfo();
                    drugInfo.setGenericName(doc.select("body > div.container > div.page-content > div > h1").text().replace(" - Drug Information", ""));
                    drugInfo.setICDcode(doc);
                    drugInfo.setTherapeuticClassification(doc);
                    drugInfo.setTradeNames(doc.select("body > div.container > div.page-content > div > div > div > a"));

                    String details = doc.select("body > div.container > div.page-content > div > div").text();
                    drugInfo.setInternationalName(details);
                    drugInfo.setWhyItIsPrescribed(details);
                    drugInfo.setWhenItIsNotBeTaken(details);
                    drugInfo.setPregnancyCategory(details);
                    drugInfo.setCategory();
                    drugInfo.setDosageAndWhenItIsToBeTaken(details);
                    drugInfo.setHowItShouldBeTaken(details);
                    drugInfo.setWarningPrecaution(details);
                    drugInfo.setSideEffect(details);
                    drugInfo.setStorageConditions(details);

                    System.out.println(drugInfo.toString());
                }
        }
    }
}
