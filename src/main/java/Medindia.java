import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

public class Medindia {
    private Map<String, String> loginCookies = new LinkedHashMap<>();
    private LinkedBlockingQueue<Proxy> proxies = new LinkedBlockingQueue<>();

    public Medindia() throws InterruptedException {
        loginCookies.put("ASPSESSIONIDQCSCRDDT", "LKCCNEADMGGFJJIIJHDHAPCE");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    File folder = new File("proxies");
                    folder.mkdirs();
                    File[] listOfFiles = folder.listFiles();

                    if (listOfFiles != null) {
                        for (File listOfFile : listOfFiles) {
                            if (listOfFile.isFile()) {
                                try (BufferedReader br = new BufferedReader(new FileReader(listOfFile))) {
                                    String proxyLine;
                                    while ((proxyLine = br.readLine()) != null) {
                                        if (!proxyLine.isEmpty()) {
                                            String[] res = proxyLine.split(":");
                                            proxies.put(new Proxy(res[0].trim(), res[1].trim()));
                                            System.err.println("Added new proxy address. Available proxies:" + proxies.size());
                                        }
                                    }
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            listOfFile.delete();
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void changeProxy(Proxy proxy) {
        System.setProperty("http.proxyHost", proxy.getHost());
        System.setProperty("http.proxyPort", proxy.getPort());
        System.err.println(String.format("Changed proxy to %s:%s; Available proxies: %s", proxy.getHost(), proxy.getPort(), proxies.size()));
    }

    public Document connectTo(String url) throws InterruptedException {
        Document doc = null;
        int failCounter = 0;
        while (doc == null) {
            try {
                doc = Jsoup.connect(url).cookies(loginCookies).timeout(30000).get();
                if (doc.text().contains("Please note copying of data is not allowed from Medindia") || doc.text().contains("Captcha | Medindia")){
                    doc = null;
                    throw new IOException("Blocked");
                }
                if (doc.select("body > div.container > div.page-content > div > h1").text().isEmpty()){
                    System.out.println(doc.text());
                    doc = null;
                    throw new IOException("Page is empty");
                }
            } catch (IOException e) {
                System.err.println(e.getMessage() + " : " + failCounter + "; Available proxies:" + proxies.size());
                if (++failCounter % 5 == 0) {
                    changeProxy(proxies.take());
                }
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
            int drugNo = 0;
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

                System.out.println(String.format("SNo: %s; Link: %s; \n%s", ++drugNo, drug.attr("href"), drugInfo.toString()));
            }
        }
    }
}
