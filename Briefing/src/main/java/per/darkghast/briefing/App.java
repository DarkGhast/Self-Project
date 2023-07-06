package per.darkghast.briefing;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import per.darkghast.briefing.exception.PublicAccountUndefinedException;
import per.darkghast.briefing.util.UpdateCheckUtil;
import per.darkghast.briefing.util.UserAgentUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
@Slf4j
public class App {
    private static final String PUBLIC_ACCOUNT_NAME = "易即今日";
    private static final String URL = "https://weixin.sogou.com/weixin";

    public static void main(String[] args) throws IOException {
        String userAgent = UserAgentUtil.randomUserAgent();
        Map<String, String> headers = getNewHeaders(userAgent);
        List<HttpCookie> cookies = getNewCookies(userAgent);
        String relativeUrl = search(cookies, headers);
        String absoluteUrl = urlDecode(relativeUrl);
        File newsPicture = getPicture(absoluteUrl, cookies, headers);
        log.info("新闻已更新，size={}", newsPicture.length());
    }

    /**
     * 构建请求头部
     *
     * @param ua UserAgent
     * @return 请求头
     */
    private static Map<String, String> getNewHeaders(String ua) {
        Map<String, String> headers = new HashMap<>(16);
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;"
                + "q=0.9,image/webp,image/apng,*/*;"
                + "q=0.8,application/signed-exchange;"
                + "v=b3;"
                + "q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.put("User-Agent", ua);
        return headers;
    }

    /**
     * 获取Cookie
     *
     * @param ua UserAgent
     * @return Cookie
     */
    private static List<HttpCookie> getNewCookies(String ua) {
        String url = "https://v.sogou.com/v?ie=utf8&query=&p=40030600";

        HttpRequest request = HttpRequest.get(url)
                .header("User-Agent", ua)
                .setFollowRedirects(false);

        HttpResponse response = request.execute();

        return response.getCookies();
    }

    /**
     * 解析HTML文本并获取最新文章的URL
     *
     * @param html HTML文本
     * @return 最新文章的相对URL
     */
    public static String parseHtml(String html) throws IOException {
        Document doc = Jsoup.parse(html);

        List<Element> elementList = doc.select("a")
                .stream()
                .filter(dom -> "account_article_0".equals(dom.attr("uigs")))
                .collect(Collectors.toList());

        if (elementList.isEmpty()) {
            log.error("结果为空，请检查{}公众号的状态", PUBLIC_ACCOUNT_NAME);
            throw new PublicAccountUndefinedException("结果为空，请检查{}公众号的状态");
        } else {
            Element element = elementList.get(0);
            // 新闻版本 简报(X月X日)
            String version = ReUtil.findAll("简报\\(\\d+月\\d+日\\)", element.toString(), 0, new ArrayList<>()).get(0);
            UpdateCheckUtil.checkIsUpdate(version);
            return element.attr("href");
        }
    }

    /**
     * 调用搜狗微信查询接口，返回最新文章的相对路径
     *
     * @param cookies cookie列表
     * @param headers 请求头
     * @return 最新文章的相对路径
     */
    private static String search(List<HttpCookie> cookies, Map<String, String> headers) throws IOException {
        HttpRequest request = HttpRequest.get(URL)
                .headerMap(headers, false)
                .cookie(cookies)
                .form("type", "1")
                .form("s_from", "input")
                .form("ie", "uft8")
                .form("_sug_", "n")
                .form("_sug_type_", "")
                .form("query", App.PUBLIC_ACCOUNT_NAME);

        String html = request.execute().body();
        return parseHtml(html);
    }

    /**
     * 解析URL
     *
     * @param relativeUrl 相对URL
     * @return 绝对URL
     */
    private static String urlDecode(String relativeUrl) {
        String baseUrl = "https://weixin.sogou.com";
        int b = RandomUtil.randomInt(100);
        int index = relativeUrl.indexOf("url=") + 30 + b;
        String a = relativeUrl.substring(index, index + 1);
        return baseUrl + relativeUrl + "&k=" + b + "&h=" + a;
    }

    /**
     * 下载新闻图片
     *
     * @param url     新闻文章绝对URL
     * @param cookies Cookie
     * @param headers 请求头
     * @return 新闻图片文件
     */
    private static File getPicture(String url, List<HttpCookie> cookies, Map<String, String> headers) {
        HttpRequest request = HttpRequest.get(url)
                .headerMap(headers, false)
                .cookie(cookies);
        String html = request.execute().body();


        List<String> urlFragmentList = ReUtil.findAll("url \\+= '(.*?)';", html, 1, new ArrayList<>());
        String middleUrl = String.join("", urlFragmentList);
        middleUrl = StrUtil.replace(middleUrl, "http", "https");
        HttpRequest request2 = HttpRequest.get(middleUrl)
                .headerMap(headers, false)
                .cookie(cookies);
        String middleHtml = request2.execute().body();


        // 图片的CDN URL
        String cdnUrl = ReUtil.findAll("cdn_url: '(.*?)',", middleHtml, 1, new ArrayList<>()).get(0);
        File file = FileUtil.file("./output/img/news.jpg");
        HttpUtil.downloadFile(cdnUrl, file);
        return file;
    }
}

