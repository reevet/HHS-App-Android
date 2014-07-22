package info.holliston.high.app;

import info.holliston.high.app.xmlparser.ArticleParser;

public class ArticleDataSourceOptions {
    private String databaseName;
    private String urlString;
    private String[] parserNames;
    private ArticleParser.HtmlTags conversionType;
    private ArticleDataSource.SortOrder sortOrder;
    private String limit;

    public ArticleDataSourceOptions(String databaseName, String urlString, String[] parserNames, ArticleParser.HtmlTags conversionType, ArticleDataSource.SortOrder sortOrder, String limit) {
        this.setDatabaseName(databaseName);
        this.setUrlString(urlString);
        this.setParserNames(parserNames);
        this.setConversionType(conversionType);
        this.setSortOrder(sortOrder);
        this.setLimit(limit);
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public String getUrlString() {
        return this.urlString;
    }

    public String[] getParserNames() {
        return this.parserNames;
    }

    public ArticleParser.HtmlTags getConversionType() {
        return this.conversionType;
    }

    public ArticleDataSource.SortOrder getSortOrder() {
        return this.sortOrder;
    }

    public String getLimit() {
        return this.limit;
    }

    public void setDatabaseName(String name) {
        this.databaseName = name;
    }

    public void setUrlString(String url) {
        this.urlString = url;
    }

    public void setParserNames(String[] names) {
        this.parserNames = names;
    }

    public void setConversionType(ArticleParser.HtmlTags type) {
        this.conversionType = type;
    }

    public void setSortOrder(ArticleDataSource.SortOrder order) {
        this.sortOrder = order;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
}
