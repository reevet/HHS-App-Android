package info.holliston.high.app.model;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class Article implements Serializable {
    public long id;
    public String title;
    public URL url;
    public Date date;
    public String details;
    public UUID key = UUID.randomUUID();
    public String imgSrc;

    public Article () {}

    public Article (String title, URL url, Date date, String details, String imgSrc)
    {
        this.title = title;
        this.url = url;
        this.date = date;
        this.details = details;
        this.imgSrc = imgSrc;
    }
}


