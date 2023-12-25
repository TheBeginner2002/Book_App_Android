package com.example.bookapp.model;

public class ModelBook {
    String uid, id, bookTitle, bookDescription, categoryId, urlPdf;
    long timestamp;

    public ModelBook() {
    }

    public ModelBook(String uid, String id, String bookTitle, String bookDescription, String categoryId, String urlPdf, long timestamp) {
        this.uid = uid;
        this.id = id;
        this.bookTitle = bookTitle;
        this.bookDescription = bookDescription;
        this.categoryId = categoryId;
        this.urlPdf = urlPdf;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(String urlPdf) {
        this.urlPdf = urlPdf;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
