package seedu.address.logic.parser.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.contact.ContactCliSyntax.PREFIX_CONTACT_TAG_LONG;
import static seedu.address.logic.parser.event.EventCliSyntax.PREFIX_EVENT_TAG_LONG;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.commands.update.EditEventDescriptor;
import seedu.address.logic.commands.update.RemoveTagFromEventCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.contact.ContactParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.contact.Tag;

/**
 * Parses input arguments and creates a new RemoveTagFromEventCommand object.
 */
public class RemoveTagFromEventCommandParser implements Parser<RemoveTagFromEventCommand> {
    @Override
    public RemoveTagFromEventCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_EVENT_TAG_LONG);

        // Ensure only one prefix is present
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_EVENT_TAG_LONG);
        if (!argMultimap.arePrefixesPresent(PREFIX_EVENT_TAG_LONG) || argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemoveTagFromEventCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemoveTagFromEventCommand.MESSAGE_USAGE), pe);
        }

        // It is guaranteed that there is only one --tag.
        // Get the sole value, split by whitespace
        // convert to collection<String>, then parse to Set<Tag>
        Collection<String> tags = argMultimap.getValue(PREFIX_CONTACT_TAG_LONG)
                .map(s -> Arrays.asList(s.split("\\s+")))
                .orElse(Collections.emptyList());

        return new RemoveTagFromEventCommand(index, ContactParserUtil.parseTags(tags));
    }
}
