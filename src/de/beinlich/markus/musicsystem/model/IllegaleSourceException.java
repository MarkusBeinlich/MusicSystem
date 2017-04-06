/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

/**
 *
 * @author Markus Beinlich
 */
public class IllegaleSourceException extends Exception {

    /**
     *
     */
    public IllegaleSourceException() {
    }

    /**
     *
     * @param message
     */
    public IllegaleSourceException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public IllegaleSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public IllegaleSourceException(Throwable cause) {
        super(cause);
    }

}
