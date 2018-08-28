package app.devlife.connect2sql.sql;

public class Column {
    private String mName = "";
    private String mType = "";

    public Column(String name, String type) {
        mName = name;
        mType = type;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public String getType() {
        return mType;
    }
}
