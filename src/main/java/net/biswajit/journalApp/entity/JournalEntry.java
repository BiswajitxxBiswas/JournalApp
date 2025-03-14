package net.biswajit.journalApp.entity;

public class JournalEntry {

    private long id;
    private String title;
    private String content;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public String getContent(){
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
