package de.beinlich.markus.musicsystem.model;


public class MusicCollectionCreator {
    public static synchronized MusicCollection getInstance(String format) {
        return MusicCollection.getInstance(format);
    }
}
