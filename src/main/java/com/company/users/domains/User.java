package com.company.users.domains;

public class User {
    private int id;
    //name, age ,,,, etc

    public User(){}
    public User(int id){
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
