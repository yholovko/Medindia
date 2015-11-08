import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Medindia {
    private Map<String, String> loginCookies = new LinkedHashMap<>();
    private LinkedBlockingQueue<MyProxy> proxies = new LinkedBlockingQueue<>();

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
                                            if (res[1].trim().length() < 5) {
                                                proxies.put(new MyProxy(res[0].trim(), res[1].trim()));
                                                System.err.println("Added new proxy address. Available proxies:" + proxies.size());
                                            }
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

    private void changeProxy(MyProxy myProxy) {
        System.setProperty("http.proxyHost", myProxy.getHost());
        System.setProperty("http.proxyPort", myProxy.getPort());
        System.err.println(String.format("[%s] Changed myProxy to %s:%s; Available proxies: %s", Thread.currentThread().getName(), myProxy.getHost(), myProxy.getPort(), proxies.size()));
    }


    public Document connectTo2(String siteUrl, Proxy proxy) throws InterruptedException {
        Document doc = null;
        int failCounter = 0;

        while (doc == null) {
            try {
                URL url = new URL(siteUrl);
                HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);
                uc.setConnectTimeout(30000);
                uc.setReadTimeout(30000);
                uc.connect();

                String line;
                StringBuffer tmp = new StringBuffer();
                BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                while ((line = in.readLine()) != null) {
                    tmp.append(line);
                }

                doc = Jsoup.parse(String.valueOf(tmp));

                if (doc.text().contains("Please note copying of data is not allowed from Medindia") || doc.text().contains("Captcha | Medindia")) {
                    doc = null;
                    throw new IOException("Blocked");
                }
                if (doc.select("body > div.container > div.page-content > div > h1").text().isEmpty()) {
                    String docText = doc.text();
                    doc = null;
                    throw new IOException(docText);
                }
            } catch (IOException e) {
                System.err.println(String.format("[%s] %s: %s; Available proxies:%s", Thread.currentThread().getName(), e.getMessage(), failCounter, proxies.size()));
                Thread.sleep(1000);
                if (++failCounter % 3 == 0) {
                    try {
                        MyProxy myProxy = proxies.take();
                        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(myProxy.getHost(), Integer.valueOf(myProxy.getPort())));
                    } catch (IllegalArgumentException | InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return doc;
    }

    public Document connectTo(String url) throws InterruptedException {
        Document doc = null;
        int failCounter = 0;
        while (doc == null) {
            try {
                doc = Jsoup.connect(url).cookies(loginCookies).timeout(30000).get();
                if (doc.text().contains("Please note copying of data is not allowed from Medindia") || doc.text().contains("Captcha | Medindia")) {
                    doc = null;
                    throw new IOException("Blocked");
                }
                if (doc.select("body > div.container > div.page-content > div > h1").text().isEmpty()) {
                    String docText = doc.text();
                    doc = null;
                    throw new IOException(docText);
                }
            } catch (IOException e) {
                System.err.println(String.format("[%s] %s: %s; Available proxies:%s", Thread.currentThread().getName(), e.getMessage(), failCounter, proxies.size()));
                if (++failCounter % 5 == 0) {
                    changeProxy(proxies.take());
                }
            }
        }
        return doc;
    }

    public void getGenericList() throws InterruptedException {
        for (char page = 'a'; page <= 'z'; page++) {
            final char finalPage = page;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Started: " + Thread.currentThread().getName());
                    try {
                        List<DrugInfo> drugInfoList = parseGenericPage(String.format("index.asp?alpha=%c", finalPage));
                        Excel.writeToGenericSheet(String.valueOf(finalPage), drugInfoList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Finished: " + Thread.currentThread().getName());
                }
            }, String.format("index.asp?alpha=%c", finalPage)).start();
        }
    }

    public void getBrandedList() throws InterruptedException {
        for(char page='a'; page <= 'z'; page++) {
            final char finalPage = page;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Started: " + Thread.currentThread().getName());
                    try {
                        parseBrandPage(String.format("brand-index.asp?alpha=%c", finalPage));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Finished: " + Thread.currentThread().getName());
                }
            }, String.format("index.asp?alpha=%c", finalPage)).start();
        }
    }

    public void parseBrandPage(String page) throws InterruptedException {
        int processed = 0;
        MyProxy myProxy = proxies.take();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(myProxy.getHost(), Integer.valueOf(myProxy.getPort())));
        Excel excel = new Excel(page.replace("brand-index.asp?alpha=", ""));

        Document doc = connectTo2(Constants.BRANDS_URL + page, proxy);   //todo cookies
        Elements brands = doc.select("body > div.container > div.page-content > div > div.vertical-scroll > table > tbody > tr > td > a");
        while (true) {
            if (hasNext(doc)) {
                String nextPageUrl = doc.select("body > div.container > div.page-content > div > div.pagination > a:nth-child(8)").attr("href");
                doc = connectTo2(Constants.BRANDS_URL + nextPageUrl, proxy);
                brands.addAll(doc.select("body > div.container > div.page-content > div > div.vertical-scroll > table > tbody > tr > td > a"));
                System.out.println(String.format("Connected to %s; Total brands: %s", nextPageUrl, brands.size()));
                Thread.sleep(2000);
            } else {
                break;
            }
        }

        for (Element brand : brands) {
            doc = connectTo2(Constants.BRANDS_URL + brand.attr("href"), proxy);

            BrandInfo brandInfo = new BrandInfo();
            brandInfo.setBrandName(doc.select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(2).text());
            brandInfo.setGenericName(doc.select("body > div.container > div.breadcrumb > a:nth-child(3)").text());
            brandInfo.setCombinationsGenerics(doc.select("body > div.container > div.page-content > div.fluid > b").text().replaceAll("Combination of Generics - ", ""));
            brandInfo.setManufacturer(doc.select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(4).text());
            brandInfo.setUnit(doc.select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(6).text());
            brandInfo.setType(doc.select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(8).text());
            if (doc.select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").size() > 10) {
                brandInfo.setQuantity(doc.select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(10).text());
                brandInfo.setPrice(doc.select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(12).text());
            }else{
                brandInfo.setQuantity("");
                brandInfo.setPrice("");
            }

            excel.writeToBrandedSheet(brandInfo);
            System.out.println(String.format("[%s] Link: %s; Processed %s out of %s", Thread.currentThread().getName(), brand.attr("href"), ++processed, brands.size()));
        }

    }

    public List<DrugInfo> parseGenericPage(String page) throws InterruptedException {
        MyProxy myProxy = proxies.take();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(myProxy.getHost(), Integer.valueOf(myProxy.getPort())));

        List<DrugInfo> drugInfoList = new ArrayList<>();
        Document doc = connectTo2(Constants.DRUGS_URL + page, proxy);
        Elements drugs = doc.select("body > div.container > div.page-content > div > div.vertical-scroll > table > tbody > tr > td:nth-child(2) > a");

        for (Element drug : drugs) {
            doc = connectTo2(drug.attr("href"), proxy);

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

            drugInfoList.add(drugInfo);
            System.out.println(String.format("[%s] Link: %s; Processed %s out of %s", Thread.currentThread().getName(), drug.attr("href"), drugInfoList.size(), drugs.size()));
        }
        return drugInfoList;
    }

    private boolean hasNext(Document doc) {
        return doc.select("body > div.container > div.page-content > div > div.pagination > a").text().contains("Next");
    }
}