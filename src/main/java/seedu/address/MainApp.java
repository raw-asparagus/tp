package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Version;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.ConfigUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.contact.Contact;
import seedu.address.model.contact.ContactManager;
import seedu.address.model.contact.ContactManagerWithFilteredList;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventManager;
import seedu.address.model.event.EventManagerWithFilteredList;
import seedu.address.model.item.ItemInvolvingContactManager;
import seedu.address.model.item.ItemNotInvolvingContactManager;
import seedu.address.model.todo.Todo;
import seedu.address.model.todo.TodoManager;
import seedu.address.model.todo.TodoManagerWithFilteredList;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.UserPrefsStorage;
import seedu.address.storage.contact.JsonContactStorage;
import seedu.address.storage.event.JsonEventStorage;
import seedu.address.storage.todo.JsonTodoStorage;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(1, 5, 1, true);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());
        initLogging(config);

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonContactStorage contactStorage = new JsonContactStorage(userPrefs.getContactListFilePath());
        JsonTodoStorage todoStorage = new JsonTodoStorage(userPrefs.getTodoListFilePath());
        JsonEventStorage eventStorage = new JsonEventStorage(userPrefs.getEventListFilePath());
        storage = new StorageManager(contactStorage, todoStorage, eventStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic);
    }
    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * or an empty address book will be used instead if errors occur when reading {@code storage}'s address book.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ItemNotInvolvingContactManager<Contact>> addressBookOptional;
        ItemNotInvolvingContactManager<Contact> initialContactData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (!addressBookOptional.isPresent()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with a sample AddressBook.");
            }
            initialContactData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty AddressBook.");
            initialContactData = new ContactManager();
        }

        Optional<ItemInvolvingContactManager<Todo>> todoListOptional;
        ItemInvolvingContactManager<Todo> initialTodoData;
        try {
            todoListOptional = storage.readTodoList(initialContactData);
            if (!todoListOptional.isPresent()) {
                logger.info("Creating a new data file " + storage.getTodoListFilePath()
                        + " populated with a sample Todo list.");
            }
            initialTodoData = todoListOptional.orElseGet(SampleDataUtil::getSampleTodoList);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getTodoListFilePath() + " could not be loaded."
                    + " Will be starting with an empty Todo list.");
            initialTodoData = new TodoManager();
        }

        Optional<ItemInvolvingContactManager<Event>> eventListOptional;
        ItemInvolvingContactManager<Event> initialEventData;
        try {
            eventListOptional = storage.readEventList(initialContactData);
            if (!eventListOptional.isPresent()) {
                logger.info("Creating a new data file " + storage.getEventListFilePath()
                        + " populated with a sample Event list.");
            }
            initialEventData = eventListOptional.orElseGet(SampleDataUtil::getSampleEventList);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getEventListFilePath() + " could not be loaded."
                    + " Will be starting with an empty Event list.");
            initialEventData = new EventManager();
        }

        return new ModelManager(
                userPrefs,
                new ContactManagerWithFilteredList(initialContactData),
                new TodoManagerWithFilteredList(initialTodoData),
                new EventManagerWithFilteredList(initialEventData)
        );
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            if (!configOptional.isPresent()) {
                logger.info("Creating new config file " + configFilePathUsed);
            }
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataLoadingException e) {
            logger.warning("Config file at " + configFilePathUsed + " could not be loaded."
                    + " Using default config properties.");
            initializedConfig = new Config();
        }

        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (!prefsOptional.isPresent()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
