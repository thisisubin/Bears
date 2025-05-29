package test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class JsonTest {
    public static void main(String[] args) throws IOException {
        String url = "https://pann.nate.com/";
        Document document = Jsoup.connect(url).get();
        //System.out.println(document);
        //.class #id
        // 클래스 s-talker -> 클래스 post_list
        Elements elements = document.select(".s-talker .post_list"); //s-talker하나에 post_list를 뺀다
        //System.out.println(elements.text());
        for (Element e : elements) {
            //System.out.println(e.text());
            Elements elements1 = e.children();
            for (Element e1 : elements1) {
                System.out.println(e1.text());

            }
        }
    }
}
