package com.fitwise.listeners.block.program;

import com.fitwise.utils.mail.AsyncMailerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * Created by Vignesh G on 29/06/20
 */
@Component
@RequiredArgsConstructor
public class MailProgramBlockListener implements ProgramBlockListener {

    private final AsyncMailerUtil asyncMailerUtil;

    /**
     * Mail related events for program block
     *
     * @param programBlockEvent
     */
    @Override
    public void programBlocked(ProgramBlockEvent programBlockEvent) {
        asyncMailerUtil.triggerProgramBlockMail(programBlockEvent);
    }

    /**
     * Mail related events for program unblock
     *
     * @param programBlockEvent
     */
    @Override
    public void programUnblocked(ProgramBlockEvent programBlockEvent) {
        asyncMailerUtil.triggerProgramUnblockMail(programBlockEvent);

    }
}
