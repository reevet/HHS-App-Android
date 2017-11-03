package info.holliston.high.app.datamodel.download;

    /*
     * A custom object for holding all of the datasource options
     */

public class ArticleDataSourceOptions {
    protected SortOrder sortOrder;
    private String databaseName;
    private SourceType sourceType;
    private String urlString;
    private String[] parserNames;
    private ArticleParser.HtmlTags conversionType;
    private String limit;

    public ArticleDataSourceOptions(String databaseName, SourceType sourceType,
                                    String urlString, String[] parserNames,
                                    ArticleParser.HtmlTags conversionType,
                                    SortOrder sortOrder, String limit) {
        this.setDatabaseName(databaseName);
        this.setSourceType(sourceType);
        this.setUrlString(urlString);
        this.setParserNames(parserNames);
        this.setConversionType(conversionType);
        this.setSortOrder(sortOrder);
        this.setLimit(limit);
    }

    public String getDatabaseName() {
        return this.databaseName;
    }
    public void setDatabaseName(String name) {
        this.databaseName = name;
    }

    public String getUrlString() {
        return this.urlString;
    }
    public void setUrlString(String url) {
        this.urlString = url;
    }

    public String[] getParserNames() {
        return this.parserNames;
    }
    public void setParserNames(String[] names) {
        this.parserNames = names;
    }

    public void setSourceType(SourceType type) {
        this.sourceType = type;
    }

    public ArticleParser.HtmlTags getConversionType() {return this.conversionType;}

    public void setConversionType(ArticleParser.HtmlTags type) {
        this.conversionType = type;
    }

    public void setSortOrder(SortOrder order) {
        this.sortOrder = order;
    }

    public String getLimit() {
        return this.limit;
    }
    public void setLimit(String limit) {
        this.limit = limit;
    }

    public enum SortOrder {GET_FUTURE, GET_PAST}
    public enum SourceType {XML, JSON}


}
