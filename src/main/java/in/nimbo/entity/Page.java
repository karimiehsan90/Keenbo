package in.nimbo.entity;

import java.util.List;

public class Page {
    private String contentWithTags;
    private String contentWithOutTags;
    private List<Anchor> anchors;
    private List<Meta> metas;
    private String link;
    private double pageRate;
    private String reversedLink;

    public Page(String contentWithTags, String contentWithOutTags, List<Anchor> anchors, List<Meta> metas,
                String link, double pageRate, String reversedLink) {
        this.contentWithTags = contentWithTags;
        this.contentWithOutTags = contentWithOutTags;
        this.anchors = anchors;
        this.metas = metas;
        this.link = link;
        this.pageRate = pageRate;
        this.reversedLink = reversedLink;
    }

    public String getReversedLink() {
        return reversedLink;
    }

    public String getLink() {
        return link;
    }

    public double getPageRate() {
        return pageRate;
    }

    public String getContentWithTags() {
        return contentWithTags;
    }

    public String getContentWithOutTags() {
        return contentWithOutTags;
    }

    public List<Anchor> getAnchors() {
        return anchors;
    }

    public List<Meta> getMetas() {
        return metas;
    }
}
