package de.beinlich.markus.musicsystem.model.db;

// Datensatz-Klasse
// DTO - data transfer object
// entity class

/**
 *
 * @author IBB Teilnehmer
 */
public class Radio {

    private String title;
    private String frequency;

    /**
     *
     * @param title
     * @param frequency
     */
    public Radio(String title, String frequency) {
        this.title = title;
        this.frequency = frequency;
    }
    @Override
    public String toString() {
        return getTitle() + " at " + getFrequency();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the frequency
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
