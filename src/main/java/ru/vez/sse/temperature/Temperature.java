package ru.vez.sse.temperature;

public class Temperature {

    private final double value;

    public Temperature(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "temp: " + value + 'C';
    }

    public double getValue() {
        return this.value;
    }
}
