package info.holliston.high.app.datamodel;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

public class Article implements Serializable {
    public long id;
    public String title;
    public URL url;
    public Date date;
    public String details;
    public String key;
    public String imgSrc;

    public Article () {}
    public Article (String title, String key, URL url, Date date, String details, String imgSrc)
    {
        this.title = title;
        this.url = url;
        this.date = date;
        this.details = details;
        this.imgSrc = imgSrc;

        //The key is simply the referring URL, safely stored
        //This is used mostly for caching of images
        String tempKey = "";
        if (key!=null) {
            key = key.replace("/", "");
            key = key.replace("&", "");
            key = key.replace("?", "");
            tempKey = key;
            //Log.d("Article","New key: "+key);
        }
        this.key = tempKey;
    }
}


