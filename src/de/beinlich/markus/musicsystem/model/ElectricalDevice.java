/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.io.*;

/**
 *
 * @author Markus Beinlich
 */
public abstract class ElectricalDevice implements Serializable{

    private String name;
    private boolean power;
    private boolean onOffSwitch;
    private String location;
    
    /**
     *
     * @param name
     */
    public ElectricalDevice() {
        this("Elektro1");
    }
    public ElectricalDevice(String name) {
        this(name, "Sonstwo");
    }

    /**
     *
     * @param name
     * @param location
     */
    public ElectricalDevice(String name, String location) {
        this(name, location, false);
    }

    /**
     *
     * @param name
     * @param location
     * @param power
     */
    public ElectricalDevice(String name, String location, boolean power) {
        this(name, location, power, false);
    }

    /**
     *
     * @param name
     * @param location
     * @param power
     * @param onOffSwitch
     */
    public ElectricalDevice(String name, String location, boolean power, boolean onOffSwitch) {
        this.name = name;
        this.location = name;
        this.power = power;
        this.onOffSwitch = onOffSwitch;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the onOffSwitch
     */
    public boolean isOnOffSwitch() {
        return onOffSwitch;
    }

    /**
     * @param onOffSwitch the onOffSwitch to set
     */
    public void setOnOffSwitch(boolean onOffSwitch) {
        if (onOffSwitch == true & this.isPower() == false){
            System.out.println(System.currentTimeMillis() + "Bitte erst für Strom sorgen. Gerät kann nicht eingeschaltet werden.");
        } else {
            this.onOffSwitch = onOffSwitch;
            System.out.println(System.currentTimeMillis() + this.getName() + " Gerät ist " + (power ? "an." : "aus."));
        }
    }

    /**
     * @return the power
     */
    public boolean isPower() {
        return power;
    }

    /**
     * @param power the power to set
     */
    public void setPower(boolean power) {
        this.power = power;
        System.out.println(System.currentTimeMillis() + this.getName() + " Strom ist " + (power ? "an." : "aus."));
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "\nGerätename: " + this.getName()
                + "\nStandort: " + location
                + "\nStrom vorhanden: " + power
                + "\nGerät eingeschaltet:" + onOffSwitch;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
