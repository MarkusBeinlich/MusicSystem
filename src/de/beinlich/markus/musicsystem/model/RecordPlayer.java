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
class RecordPlayer extends AbstractMusicPlayer {

    /**
     *
     */
    public RecordPlayer() {
        this(new Record());
    }

    /**
     *
     * @param record
     */
    public RecordPlayer(Record record) {
        if (record == null) {
            setRecord(new Record());
        } else {
            setRecord(record);
        }
        setMusicSystemState(MusicSystemState.STOP);
    }
    
    /**
     *
     */
    @Override
    public void previous() {
        // TODO Automatisch generierter Methodenstub
        System.out.println(System.currentTimeMillis() + "Das geht bei Schallplatten nicht.");
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean hasPlay() {
        return true;
    }

    @Override
    public boolean hasStop() {
       return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasPause() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasTracks() {
         return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasCurrentTime() {
         return true;
    }
}
