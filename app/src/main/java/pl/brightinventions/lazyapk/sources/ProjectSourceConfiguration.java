package pl.brightinventions.lazyapk.sources;

import java.io.Serializable;

public class ProjectSourceConfiguration implements Serializable {
    private String address;
    private String userName;
    private String password;

    public ProjectSourceConfiguration() {
        address = "http://";
    }

    public String getAddress() {
        return address;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ProjectSourceConfiguration{" +
                "address='" + address + '\'' +
                '}';
    }
}
