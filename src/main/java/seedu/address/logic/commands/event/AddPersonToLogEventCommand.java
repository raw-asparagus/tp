package seedu.address.logic.commands.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.EVENT_COMMAND_WORD;
import static seedu.address.logic.parser.event.EventCliSyntax.PREFIX_LINKED_PERSON_INDEX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.EventMessages;
import seedu.address.logic.Messages;
import seedu.address.logic.abstractcommand.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;

/**
 * Adds the log of given contacts via index.
 */
public class AddPersonToLogEventCommand extends EditCommand<Event> {
    public static final String COMMAND_WORD = "log";

    public static final String MESSAGE_USAGE = EVENT_COMMAND_WORD + " " + COMMAND_WORD
            + ": Logs some contacts with an event.\n"
            + "Parameters: INDEX "
            + PREFIX_LINKED_PERSON_INDEX + " [PERSON_INDEX]...\n"
            + "Example: " + EVENT_COMMAND_WORD + " " + COMMAND_WORD + " 1 "
            + PREFIX_LINKED_PERSON_INDEX + " 1 2 3";
    public static final String MESSAGE_ADD_LOG_SUCCESS = "Added attendence from persons to event: %1$s";
    public static final String MESSAGE_PERSON_ALREADY_LOGGED =
            "The person at the following index(es) are already logged: %s";

    private final List<Index> personIndices;

    /**
     * Creates a RemovePersonFromEventCommand to remove the persons at the specific {@code
     * personIndices}.
     */
    public AddPersonToLogEventCommand(Index index, List<Index> personIndices) {
        super(index, Model::getEventManagerAndList);
        requireNonNull(personIndices);
        this.personIndices = personIndices;
    }

    @Override
    public Event createEditedItem(Model model, Event eventToEdit) throws CommandException {
        for (Index index : personIndices) {
            if (index.getZeroBased() >= eventToEdit.getPersons().size()) {
                throw new CommandException(String.format(EventMessages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                        index.getOneBased()));
            }
        }
        List<Boolean> newMarkList = new ArrayList<>(eventToEdit.getMarkedList());
        List<Index> checkPersonMarked = personIndices.stream()
                .filter(x -> newMarkList.get(x.getZeroBased()))
                .toList();
        if (!checkPersonMarked.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_PERSON_ALREADY_LOGGED,
                    checkPersonMarked.stream()
                            .map(x -> String.valueOf(x.getOneBased()))
                            .collect(Collectors.joining(", ")))
            );
        }
        personIndices.stream()
                .map(Index::getZeroBased)
                .sorted(Comparator.reverseOrder())
                .forEach(index -> newMarkList.set(index, true));
        return new Event(
                eventToEdit.getName(),
                eventToEdit.getStartTime(),
                eventToEdit.getEndTime(),
                eventToEdit.getLocation(),
                eventToEdit.getPersons(),
                List.copyOf(newMarkList),
                eventToEdit.getTags()
        );
    }

    @Override
    public String getInvalidIndexMessage() {
        return EventMessages.MESSAGE_INVALID_EVENT_DISPLAYED_INDEX;
    }

    @Override
    public String getDuplicateMessage() {
        return EventMessages.MESSAGE_DUPLICATE_EVENT;
    }

    @Override
    public String getSuccessMessage(Event editedItem) {
        return String.format(MESSAGE_ADD_LOG_SUCCESS, Messages.format(editedItem));
    }
}
