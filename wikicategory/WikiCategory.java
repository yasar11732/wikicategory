/**
 * WikiCategory
 *
 * There is no copyright, do whatever you want with this.
 * @author Yaşar Arabacı
 * @date 10.08.2013
 */
package wikicategory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Given a category like "Category:Psychology" and an endpoint like
 * http://en.wikipedia.org/w/api.php this class will attempt to get all articles
 * belonging to that category. it can write those to a file, or give you a
 * JSONObject of all the articles.
 *
 * @author yasar
 */
public class WikiCategory {

    private static final String EXPLANATION = "Export a category";
    private String userAgent;
    private String endPoint;
    private String category;
    private JSONObject pages;
    private HashMap<String, String> params;

    /**
     * @param name Your name
     * @param eMail your e-mail
     * @param endPoint meadia wiki api endpoint e.g.
     * http://en.wikipedia.org/w/api.php
     * @param category The category you would like to get. e.g
     * "Category:Psychology"
     */
    public WikiCategory(String name, String eMail, String endPoint, String category) {
        this(name, eMail, endPoint);
        this.category = category;
        this.params.put("gcmtitle", this.category);
    }

    /**
     * please check
     * {@link #WikiCategory(String, String, String, String) WikiCategory}
     */
    public WikiCategory(String name, String eMail) {
        this.params = new HashMap<>();
        this.params.put("generator", "categorymembers");
        this.params.put("gcmtype", "page");
        this.params.put("prop", "revisions");
        this.params.put("rvprop", "content");
        // params.put("rvparse","1"); // to get html output

        this.userAgent = String.format("%s -- Java %s, %s %s", EXPLANATION, System.getProperty("java.version"), name, eMail);
    }

    /**
     * please check
     * {@link #WikiCategory(String, String, String, String) WikiCategory}
     */
    public WikiCategory(String name, String eMail, String endPoint) {
        this(name, eMail);
        this.endPoint = endPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /* DEBUGGINGG PURPOSES ONLY 
     public static void main(String[] args) {
     WikiCategory wk = new WikiCategory("Yaşar Arabacı", "yasar11732@gmail.com",
     "http://en.wikipedia.org/w/api.php", "Category:Psychology");
     wk.run();
     wk.saveToFiles(".txt");

     // System.out.println("function returned: " + pages);

     } /**/
    /**
     * calls {@link #saveToFiles(File, String) saveToFiles} with targetDir set
     * to current directory and suffix set to "".
     */
    public void saveToFiles() {
        saveToFiles(new File(System.getProperty("user.dir"), "wiki"), "");
    }

    /**
     * calls {@link #saveToFiles(File, String) saveToFiles} with targetDir set
     * to current directory/wiki and suffix set to fileSuffix.
     */
    public void saveToFiles(String fileSuffix) {
        saveToFiles(new File(System.getProperty("user.dir"), "wiki"), fileSuffix);
    }

    /**
     * After calling {@link #run() run} you can call this function to save
     * retrieved pages to files in disk
     *
     * @param targetDir where to save the files
     * @param fileSuffix it will be used as file extension
     */
    public void saveToFiles(File targetDir, String fileSuffix) {
        // System.out.println(this.pages);

        if (targetDir.exists() && !targetDir.isDirectory()) {
            return;
        }
        if (!targetDir.exists() && !targetDir.mkdir()) {
            return;
        }

        for (Iterator it = this.pages.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            JSONObject current = (JSONObject) this.pages.get(key);
            File destFile = new File(targetDir, URLEncoder.encode(current.get("title").toString() + fileSuffix));
            System.out.println("File will be" + destFile);
            JSONArray revisions = (JSONArray) current.get("revisions");
            JSONObject firstRev = (JSONObject) revisions.get(0);
            try {
                if (!destFile.exists() && !destFile.createNewFile()) {
                    System.out.println("Couldn't create target file:" + destFile);
                    return;
                }
            } catch (IOException ex) {
                Logger.getLogger(WikiCategory.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            try {
                PrintWriter writer = new PrintWriter(destFile, "UTF-8");
                writer.print((String) firstRev.get("*"));
                writer.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(WikiCategory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(WikiCategory.class.getName()).log(Level.SEVERE, null, ex);
            }



        }
    }

    private HashMap<String, String> getParams() {
        return this.params;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    private static String makeQueryString(HashMap<String, String> args) {
        StringBuilder sb = new StringBuilder();
        for (String key : args.keySet()) {
            sb.append(URLEncoder.encode(key));
            sb.append("=");
            sb.append(URLEncoder.encode(args.get(key)));
            sb.append("&");
        }
        /* there is an extra "&" at the end*/
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
    
    /**
     * call this method to fetch data from mediawiki api
     * @return a JSONObject that contains pages returned from api call
     */
    public JSONObject run() {
        if (this.endPoint == null || this.category == null) {
            System.err.println("endpoint or category is not set");
            return null;
        }
        JSONObject query = get("query", this.getParams());
        JSONObject currentPages = (JSONObject) ((JSONObject) query.get("query")).get("pages");

        while (query.get("query-continue") != null) {
            HashMap<String, String> nparams = new HashMap<>(this.params);
            JSONObject queryContinue = (JSONObject) query.get("query-continue");
            JSONObject categoryMembers = (JSONObject) queryContinue.get("categorymembers");
            String gcmContinue = (String) categoryMembers.get("gcmcontinue");
            nparams.put("gcmcontinue", gcmContinue);
            query = get("query", nparams);
            currentPages.putAll((JSONObject) ((JSONObject) query.get("query")).get("pages"));
        }
        this.pages = currentPages;
        return this.pages;
    }

    private JSONObject get(String action, HashMap<String, String> queryArgs) {

        queryArgs.put("action", action);
        queryArgs.put("format", "json");

        String queryString = makeQueryString(queryArgs);
        String url = String.format("%s?%s", this.endPoint, queryString);
        System.out.println(url);

        try {
            URL urlObj = new URL(url);
            JSONParser parser = new JSONParser();

            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", this.userAgent);
            Object obj = parser.parse(new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8")));
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.get("error") != null) {
                System.err.println(jsonObject.get("error"));
                return null;
            } else {
                return jsonObject;
            }

        } catch (IOException e) {
            System.err.println("Couldn't establish http connection" + e.getMessage());
            return null;
        } catch (ParseException pe) {
            System.err.println("Couldn't parse returned data as json:" + pe.getPosition());
            return null;
        }

    }
}
