package com.bridgelabz.fundoonotes.messaging;

import com.bridgelabz.fundoonotes.dto.EmailDto;
import com.bridgelabz.fundoonotes.model.Note;
import com.bridgelabz.fundoonotes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class ReminderScheduler {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private MessageProducer messageProducer;

    // Scheduled to run every minute
    @Scheduled(cron = "0 * * * * *")
    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        
        // In a production environment, this should be optimized with a specific DB query.
        // For this implementation, we check all active notes.
        List<Note> allNotes = noteRepository.findAll();
        
        for (Note note : allNotes) {
            if (!note.isTrashed() && note.getReminderDate() != null) {
                LocalDateTime reminder = note.getReminderDate();
                
                // If the current time matches the reminder time (up to the minute)
                if (reminder.getYear() == now.getYear() &&
                    reminder.getMonthValue() == now.getMonthValue() &&
                    reminder.getDayOfMonth() == now.getDayOfMonth() &&
                    reminder.getHour() == now.getHour() &&
                    reminder.getMinute() == now.getMinute()) {
                    
                    EmailDto emailDto = new EmailDto(
                            note.getUser().getEmail(), 
                            "Fundoo Notes Reminder: " + note.getTitle(),
                            "Hi " + note.getUser().getFirstName() + ",\n\nHere is your reminder for the note:\n" + note.getDescription()
                    );
                    
                    messageProducer.sendEmailMessage(emailDto);
                    
                    // Clear the reminder after it triggers so it doesn't fire again
                    note.setReminderDate(null);
                    noteRepository.save(note);
                }
            }
        }
    }
}
