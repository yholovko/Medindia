package com.medindia.elance;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Medindia {
    private Map<String, String> loginCookies = new LinkedHashMap<>();
    private LinkedBlockingQueue<MyProxy> proxies = new LinkedBlockingQueue<>();
    private char[] specifiedPages;

    public Medindia(char[] specifiedPages) throws InterruptedException {
        this.specifiedPages = specifiedPages;

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
                                                System.out.println("Added new proxy address. Available proxies:" + proxies.size());
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println(Thread.currentThread().getName() + " \n" + e.getMessage());
                                }
                                try {
                                    new PrintWriter(listOfFile).close(); //delete all content
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    System.out.println(Thread.currentThread().getName() + " \n" + e.getMessage());
                                }
                            }
                            //listOfFile.delete();
                        }
                    }
                    try {
                        Thread.currentThread().sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println(Thread.currentThread().getName() + " \n" + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void changeProxy(MyProxy myProxy) {
        System.setProperty("http.proxyHost", myProxy.getHost());
        System.setProperty("http.proxyPort", myProxy.getPort());
        System.out.println(String.format("[%s] Changed myProxy to %s:%s; Available proxies: %s", Thread.currentThread().getName(), myProxy.getHost(), myProxy.getPort(), proxies.size()));
    }


    public Document connectTo2(String siteUrl, Proxy proxy) throws InterruptedException {
        Document doc = null;
        int failCounter = 0;

        while (doc == null) {
            try {
                CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

                URL url = new URL(siteUrl);
                HttpURLConnection uc;
                if (proxy != null) {
                    uc = (HttpURLConnection) url.openConnection(proxy);
                } else {
                    uc = (HttpURLConnection) url.openConnection();
                }
                uc.setConnectTimeout(30000);
                uc.setReadTimeout(30000);
                uc.setRequestProperty("Cookie", "ASPSESSIONIDCCDASDDS=FEJHNEEDGNLJMHPKHFGMBKNH;" + "ASPSESSIONIDSACCSAAT=JMFMJKKDDACDEGOJOMEDCNCC;" +
                        "__asc=d1963ea8150e95870df15d9ca7b;" +
                        "__auc=d1963ea8150e95870df15d9ca7b;" +
                        "__gads=ID=bc7be7ea08f039cc:T=1447023890:S=ALNI_Mbo3zOjLfHO4ELFX8d3euVJSxB1hA;" +
                        "__qca=P0-1384522194-1447023898739;" +
                        "_ga=GA1.2.1942804992.1447023898;" +
                        "_gat=1;" +
                        "_ljtrtb_10=1041809261449922086;" +
                        "_ljtrtb_12=6005293703351582048;" +
                        "_ljtrtb_16=637a5510-13f5-4592-9b2f-673ce4874fd2;" +
                        "_ljtrtb_23=CAESEEQ55C73Fysd7rNrnXf5wyk;" +
                        "_ljtrtb_27=a595333a-6421-4f88-9fb8-214c20674779;" +
                        "_ljtrtb_29=BNmY2bKjtHW6wUtui6ygg2A2vHoe;" +
                        "_ljtrtb_49=KbU3NMFQZTK3;" +
                        "ctag=68:1447110291|69:1447110291|70:1447110291|67:1447110291|72:1447110291|73:1447110291|74:1447110291|61:1447110291;" +
                        "ljt_reader=ed4f3c5fdfea29dfe6fbaee94b632621;" +
                        "ljtrtb=eJwVkEtPAkEQhP%2FLHjzZyXRPP2a8jQQkoiREwcdtd9khgmiCKCHG%2F%2B7Mteqrqk7%2FNr65aqQTFfUZ1pwj8OAcdDH04PshDL31PLSuuWw4FnbWLf38frJ4fZz5oqErGjrG4CIpMsdI5IJWqzgWDIOaooohIzusIaqjozR%2BGI8XIiPzk%2FPX2g7zw8dzltN5V8OhIKstLOHufXG755RujDPuUkqj6YUKVkYLo95aEXSAPguwRILYUQa1cj2HElpTnbTCthLFe9%2BCMiFwDgFi7gIQck9Ojc1i7aXa65xQ9Oa8F5RAjkOtqS%2B4nu9fqJttj9MnPS2P32963mwo0c%2F0c2j%2B%2FgGmhVIS;" +
                        "ljtrtb_refresh=false;" +
                        "tpro=eJxNkNtqhDA");
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
            } catch (Exception e) {
                if (!(e instanceof NoRouteToHostException)) {
                    if (++failCounter % 3 == 0) {
                        System.out.println(String.format("[%s] %s: %s; Available proxies:%s", Thread.currentThread().getName(), e.getMessage(), failCounter, proxies.size()));
                        try {
                            MyProxy myProxy = proxies.take();
                            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(myProxy.getHost(), Integer.valueOf(myProxy.getPort())));
                            Thread.currentThread().sleep(2000);
                        } catch (IllegalArgumentException | InterruptedException e1) {
                            e1.printStackTrace();
                            System.out.println(Thread.currentThread().getName() + " \n" + e1.getMessage());
                        }
                    }
//
//                    if (failCounter == 300) {
//                        return null;
//                    }
                } else {
                    System.err.println("Check your internet connection");
                    Thread.sleep(3000);
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
                if (++failCounter % 5 == 0) {
                    System.out.println(String.format("[%s] %s: %s; Available proxies:%s", Thread.currentThread().getName(), e.getMessage(), failCounter, proxies.size()));
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
        if (specifiedPages.length == 0) {
            for (char page = 'a'; page <= 'z'; page++) {
                final char finalPage = page;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Started: " + Thread.currentThread().getName());
                        try {
                            parseBrandPage(String.format("brand-index.asp?alpha=%c", finalPage));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println(Thread.currentThread().getName() + " \n" + e.getMessage());
                        }
                        System.out.println("Finished: " + Thread.currentThread().getName());
                    }
                }, String.format("index.asp?alpha=%c", finalPage)).start();
            }
        } else {
            for (char page : specifiedPages) {
                final char finalPage = page;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Started: " + Thread.currentThread().getName());
                        try {
                            parseBrandPage(String.format("brand-index.asp?alpha=%c", finalPage));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println(Thread.currentThread().getName() + " \n" + e.getMessage());
                        }
                        System.out.println("Finished: " + Thread.currentThread().getName());
                    }
                }, String.format("index.asp?alpha=%c", finalPage)).start();
            }
        }
    }

    public void parseBrandPage(String page) throws InterruptedException {
        MyProxy myProxy = proxies.take();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(myProxy.getHost(), Integer.valueOf(myProxy.getPort())));
        Excel excel = new Excel(page.replace("brand-index.asp?alpha=", ""));

        final Document[] doc = {connectTo2(Constants.BRANDS_URL + page, null)};
        if (doc[0] != null) {
            Elements brands = doc[0].select("body > div.container > div.page-content > div > div.vertical-scroll > table > tbody > tr > td > a");
            while (true) {
                if (hasNext(doc[0])) {
                    String nextPageUrl = doc[0].select("body > div.container > div.page-content > div > div.pagination > a").select("a[title=next Page]").attr("href");
                    doc[0] = connectTo2(Constants.BRANDS_URL + nextPageUrl, null);
                    if (doc[0] != null) {
                        brands.addAll(doc[0].select("body > div.container > div.page-content > div > div.vertical-scroll > table > tbody > tr > td > a"));
                        System.out.println(String.format("Connected to %s; Total brands: %s", nextPageUrl, brands.size()));
                    } else {
                        System.out.println(Thread.currentThread().getName() + ": doc=null; Page = " + page);
                        break;
                    }
                } else {
                    break;
                }
            }

            //////CHANGE PAGE ID IN JAVA PROCESS
            int startFrom = 0;
            if (page.endsWith("c")) startFrom = 615;


            final int[] processed = {startFrom};
            ExecutorService es = Executors.newFixedThreadPool(1000);

            for (int i = startFrom; i < brands.size(); i++) {
                final int finalI = i;
                es.execute(() -> {
                    while (true) {
                        try {
                            doc[0] = connectTo2(Constants.BRANDS_URL + brands.get(finalI).attr("href"), proxy);
                            if (doc[0] != null) {
                                BrandInfo brandInfo = new BrandInfo();
                                brandInfo.setRowIndex(finalI);
                                brandInfo.setBrandName(doc[0].select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(2).text());
                                brandInfo.setGenericName(doc[0].select("body > div.container > div.breadcrumb > a:nth-child(3)").text());
                                brandInfo.setCombinationsGenerics(doc[0].select("body > div.container > div.page-content > div.fluid > b").text().replaceAll("Combination of Generics - ", ""));
                                brandInfo.setManufacturer(doc[0].select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(4).text());
                                brandInfo.setUnit(doc[0].select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(6).text());
                                brandInfo.setType(doc[0].select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(8).text());
                                if (doc[0].select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").size() > 10) {
                                    brandInfo.setQuantity(doc[0].select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(10).text());
                                    brandInfo.setPrice(doc[0].select("body > div.container > div.page-content > div.fluid > div > div > table > tbody > tr > td").get(12).text());
                                } else {
                                    brandInfo.setQuantity("");
                                    brandInfo.setPrice("");
                                }

                                excel.writeToBrandedSheet(brandInfo);
                                System.err.println(String.format("[%s] Link: %s; Processed %s out of %s", Thread.currentThread().getName(), brands.get(finalI).attr("href"), ++processed[0], brands.size()));
                            } else {
                                System.out.println(Thread.currentThread().getName() + ": doc=null; Page = " + page);
                            }
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

            es.shutdown();
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } else {
            System.out.println(Thread.currentThread().getName() + ": doc=null; Page = " + page);
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
        //return doc.select("body > div.container > div.page-content > div > div.pagination > a").text().contains("Next");
        return (doc.select("body > div.container > div.page-content > div > div.pagination > a").select("a[title=next Page]").size() == 1);
    }
}