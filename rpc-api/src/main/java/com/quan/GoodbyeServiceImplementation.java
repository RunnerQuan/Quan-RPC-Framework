package com.quan;

public class GoodbyeServiceImplementation implements GoodbyeService {
    @Override
    public String goodbye(String name) {
        return "Goodbye, " + name;
    }
}
