package com.example.springexample;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table (name = "test")
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String grade;

    public Test() {
    }

    public Long getid() {
        return id;
    }
    public void setid(Long id) {
        this.id = id; 
    }
    public String getname() {
        return name;
    }
    public void setname(String name) {
        this.name = name;
    }
    public String getgrade() {
        return grade;
    }
    public void setgrade(String grade) {
        this.grade = grade;
    }

}
