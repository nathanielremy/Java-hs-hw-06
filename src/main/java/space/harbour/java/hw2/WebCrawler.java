package space.harbour.java.hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebCrawler {
    ExecutorService executorService;
    public ConcurrentLinkedQueue<URL> toVisit = new ConcurrentLinkedQueue<>();
    public ConcurrentSkipListSet alreadyVisited = new ConcurrentSkipListSet();

    public static void main(String[] args) throws MalformedURLException {
        WebCrawler webCrawler = new WebCrawler();
        URL myURL = new URL("http://www.zmiaikou.com/cv");
        webCrawler.toVisit.add(myURL);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                run();
            }
        };
    }

    class Task implements Runnable {

        final String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        final Pattern p = Pattern.compile(regex);

        @Override
        public void run() {
            //Remove one element from the queue
            URL url = toVisit.peek();
            String content = getContentOfWebPage(url);
            Set<URL> urls = null;
            try {
                urls = (Set<URL>) extractUrlsFromString(content);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            for (URL link: urls) {
                if (alreadyVisited.contains(link)){
                    toVisit.offer(link);
                }
            }
            alreadyVisited.add(url);
        }
    }

    public static List<URL> extractUrlsFromString(final String webPageContent) throws MalformedURLException {
        final String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        final Pattern p = Pattern.compile(regex);
        List<URL> result = new ArrayList<>();
        Matcher m = p.matcher(webPageContent);
        while (m.find()) {
            result.add(new URL(m.group()));
        }
        return result;
    }

    public static String getContentOfWebPage(URL url) {
        final StringBuilder content = new StringBuilder();

        try (InputStream is = url.openConnection().getInputStream();
             InputStreamReader in = new InputStreamReader(is, "UTF-8");
             BufferedReader br = new BufferedReader(in); ) {
            String inputLine;
            while ((inputLine = br.readLine()) != null)
                content.append(inputLine);
        } catch (IOException e) {
            System.out.println("Failed to retrieve content of " + url.toString());
            e.printStackTrace();
        }

        return content.toString();
    }

    void crawl() {
        while (!toVisit.isEmpty()) {
            executorService.submit(new Task());
        }
    }
}
