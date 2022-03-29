package fr.allanh.yaya;

public class TeamSimple implements Comparable {

    private String name;
    private int id;

    public TeamSimple(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() { return id; }

    @Override
    public int compareTo(Object o) {
        if (! (o instanceof TeamSimple)) {
            return 1;
        }
        TeamSimple l = (TeamSimple) o;
        if (id == l.getId()) {
            return 0;
        }
        else if (id > l.getId()) {
            return 1;
        }
        return -1;
    }
}
