package com.fitwise.listeners.block.program;

/*
 * Created by Vignesh G on 29/06/20
 */
public interface ProgramBlockListener {

    public void programBlocked(ProgramBlockEvent programBlockEvent);

    public void programUnblocked(ProgramBlockEvent programBlockEvent);

}
