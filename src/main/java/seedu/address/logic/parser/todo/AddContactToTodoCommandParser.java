package seedu.address.logic.parser.todo;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_CONTACT_INDEX;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.update.AddContactToTodoCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.PrefixAlias;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new AddContactToTodoCommandParser object
 */
public class AddContactToTodoCommandParser implements Parser<AddContactToTodoCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the
     * AddContactToTodoCommandParser and returns a AddContactToTodoCommandParser object for
     * execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddContactToTodoCommand parse(String args) throws ParseException {
        requireNonNull(args);
        PrefixAlias contactPrefix = TodoCliSyntax.PREFIX_ALIAS_TODO_LINKED_CONTACT;
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, contactPrefix.getAll());

        // Ensure only one prefix is present
        argMultimap.verifyNoDuplicatePrefixesFor(contactPrefix.getAll());
        if (argMultimap.getValue(contactPrefix).isEmpty() || argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddContactToTodoCommand.MESSAGE_USAGE));
        }

        // Parse index of todo to edit
        Index index = ParserUtil.parseIndex(argMultimap.getPreamble());

        // Parse contact indices, duplicates are handled in parseIndices
        List<Index> contactIndices = ParserUtil.parseIndices(
                argMultimap.getValue(contactPrefix).get());

        // Check against empty and duplicate contact indices
        if (contactIndices.isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_CONTACT_INDEX);
        }

        return new AddContactToTodoCommand(index, contactIndices);
    }

}
