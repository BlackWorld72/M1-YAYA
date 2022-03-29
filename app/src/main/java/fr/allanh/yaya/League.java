package fr.allanh.yaya;

public class League implements Comparable {

    private String name;
    private int id;
    private String logo;
    private boolean isCup;

    public League(String name, int id, boolean isCup, String logo) {
        this.name = name;
        this.id = id;
        this.logo = logo;
        this.isCup = isCup;
    }

    public boolean getIsCup() {
        return isCup;
    }

    public String getName() {
        return name;
    }

    public String getLogo() { return logo; }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Object o) {
        if (! (o instanceof League)) {
            return 1;
        }
        League l = (League) o;
        if (id == l.getId()) {
            return 0;
        }
        else if (id > l.getId()) {
            return 1;
        }
        return -1;
    }
}
