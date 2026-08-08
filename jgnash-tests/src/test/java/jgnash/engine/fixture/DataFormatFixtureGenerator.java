/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2026 Craig Cavanaugh and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package jgnash.engine.fixture;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jgnash.engine.Account;
import jgnash.engine.AccountType;
import jgnash.engine.CurrencyNode;
import jgnash.engine.DataStoreType;
import jgnash.engine.DefaultCurrencies;
import jgnash.engine.Engine;
import jgnash.engine.EngineFactory;
import jgnash.engine.InvestmentTransaction;
import jgnash.engine.ReconciledState;
import jgnash.engine.SecurityNode;
import jgnash.engine.Tag;
import jgnash.engine.Transaction;
import jgnash.engine.TransactionEntry;
import jgnash.engine.TransactionFactory;
import jgnash.engine.TransactionTag;
import jgnash.engine.budget.Budget;
import jgnash.engine.jpa.SqlUtils;
import jgnash.engine.recurring.MonthlyReminder;
import jgnash.util.FileUtils;

/**
 * Generates the synthetic persistence fixtures checked into test resources.
 * Expected summaries are intentionally maintained outside this generator and
 * verified by {@link DataFormatFixtureCatalogTest} through the public engine API.
 */
public final class DataFormatFixtureGenerator {

    static final char[] FIXTURE_PASSWORD = "fixture-password".toCharArray();

    private static final List<FixtureDefinition> RICH_FIXTURES = List.of(
            new FixtureDefinition("xml-rich", "portfolio.xml", DataStoreType.XML, EngineFactory.EMPTY_PASSWORD),
            new FixtureDefinition("binary-xstream-rich", "portfolio.bxds", DataStoreType.BINARY_XSTREAM,
                    EngineFactory.EMPTY_PASSWORD),
            new FixtureDefinition("h2-page-rich", "portfolio.h2.db", DataStoreType.H2_DATABASE,
                    EngineFactory.EMPTY_PASSWORD),
            new FixtureDefinition("h2-mvstore-rich", "portfolio.mv.db", DataStoreType.H2MV_DATABASE,
                    EngineFactory.EMPTY_PASSWORD),
            new FixtureDefinition("hsqldb-rich", "portfolio.script", DataStoreType.HSQL_DATABASE,
                    EngineFactory.EMPTY_PASSWORD));

    private static final List<FixtureDefinition> PASSWORD_FIXTURES = List.of(
            new FixtureDefinition("h2-page-password", "protected.h2.db", DataStoreType.H2_DATABASE,
                    FIXTURE_PASSWORD),
            new FixtureDefinition("h2-mvstore-password", "protected.mv.db", DataStoreType.H2MV_DATABASE,
                    FIXTURE_PASSWORD),
            new FixtureDefinition("hsqldb-password", "protected.script", DataStoreType.HSQL_DATABASE,
                    FIXTURE_PASSWORD));

    private DataFormatFixtureGenerator() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the fixture catalog output directory");
        }

        Locale.setDefault(Locale.US);
        final Path catalogRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Files.createDirectories(catalogRoot);

        for (final FixtureDefinition fixture : RICH_FIXTURES) {
            prepareDirectory(catalogRoot.resolve(fixture.id));
            createFixture(catalogRoot, fixture, true);
        }

        for (final FixtureDefinition fixture : PASSWORD_FIXTURES) {
            prepareDirectory(catalogRoot.resolve(fixture.id));
            createPasswordFixture(catalogRoot, fixture);
        }

        final FixtureDefinition empty = new FixtureDefinition("xml-empty", "empty.xml", DataStoreType.XML,
                EngineFactory.EMPTY_PASSWORD);
        prepareDirectory(catalogRoot.resolve(empty.id));
        createEmptyFixture(catalogRoot, empty);

        final FixtureDefinition minimal = new FixtureDefinition("binary-xstream-minimal", "minimal.bxds",
                DataStoreType.BINARY_XSTREAM, EngineFactory.EMPTY_PASSWORD);
        prepareDirectory(catalogRoot.resolve(minimal.id));
        createFixture(catalogRoot, minimal, false);

        for (final FixtureDefinition source : RICH_FIXTURES) {
            final String corruptId = source.id.replace("-rich", "-truncated");
            final Path corruptDirectory = catalogRoot.resolve(corruptId);
            prepareDirectory(corruptDirectory);
            truncate(catalogRoot.resolve(source.id).resolve(source.fileName),
                    corruptDirectory.resolve(source.fileName.replace("portfolio", "truncated")));
        }
    }

    private static void createEmptyFixture(final Path catalogRoot, final FixtureDefinition fixture) {
        final Path dataFile = catalogRoot.resolve(fixture.id).resolve(fixture.fileName);
        final Engine engine = EngineFactory.bootLocalEngine(dataFile.toString(), EngineFactory.DEFAULT,
                fixture.password, fixture.type);
        require(engine != null, "Could not create " + fixture.id);
        try {
            engine.setCreateBackups(false);
        } finally {
            EngineFactory.closeEngine(EngineFactory.DEFAULT);
        }
    }

    private static void createFixture(final Path catalogRoot, final FixtureDefinition fixture,
                                      final boolean featureRich) throws IOException {
        final Path dataFile = catalogRoot.resolve(fixture.id).resolve(fixture.fileName);
        final Engine engine = EngineFactory.bootLocalEngine(dataFile.toString(), EngineFactory.DEFAULT,
                fixture.password, fixture.type);
        require(engine != null, "Could not create " + fixture.id);
        try {
            engine.setCreateBackups(false);

            CurrencyNode usd = engine.getCurrency("USD");
            if (usd == null) {
                usd = DefaultCurrencies.buildCustomNode("USD");
                require(engine.addCurrency(usd), "Could not add USD");
                engine.setDefaultCurrency(usd);
            }

            if (featureRich) {
                populateFeatureRichScenario(engine, dataFile, usd);
            } else {
                final Account cash = account(AccountType.CASH, usd, "Minimal Cash");
                require(engine.addAccount(engine.getRootAccount(), cash), "Could not add minimal account");
            }
        } finally {
            EngineFactory.closeEngine(EngineFactory.DEFAULT);
        }
    }

    private static void createPasswordFixture(final Path catalogRoot, final FixtureDefinition fixture)
            throws IOException {
        final FixtureDefinition unprotected = new FixtureDefinition(fixture.id, fixture.fileName, fixture.type,
                EngineFactory.EMPTY_PASSWORD);
        createFixture(catalogRoot, unprotected, false);

        final Path dataFile = catalogRoot.resolve(fixture.id).resolve(fixture.fileName);
        if (fixture.type == DataStoreType.HSQL_DATABASE) {
            setHsqlPassword(dataFile);
        } else {
            require(SqlUtils.changePassword(dataFile.toString(), EngineFactory.EMPTY_PASSWORD, fixture.password),
                    "Could not protect " + fixture.id);
        }

        final Engine engine = EngineFactory.bootLocalEngine(dataFile.toString(), EngineFactory.DEFAULT,
                fixture.password, fixture.type);
        require(engine != null, "Could not reopen protected fixture " + fixture.id);
        EngineFactory.closeEngine(EngineFactory.DEFAULT);
    }

    private static void setHsqlPassword(final Path dataFile) {
        final String url = "jdbc:hsqldb:file:" + FileUtils.stripFileExtension(dataFile.toString()) + ";user=JGNASH";
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.execute("SET PASSWORD 'fixture-password'");
            statement.execute("SHUTDOWN");
        } catch (final Exception exception) {
            throw new IllegalStateException("Could not protect HSQLDB fixture", exception);
        }
    }

    private static void populateFeatureRichScenario(final Engine engine, final Path dataFile,
                                                    final CurrencyNode usd) throws IOException {
        final CurrencyNode eur = DefaultCurrencies.buildCustomNode("EUR");
        require(engine.addCurrency(eur), "Could not add EUR");

        final Account assets = account(AccountType.ASSET, usd, "Assets");
        final Account checking = account(AccountType.CHECKING, usd, "Checking");
        final Account euroWallet = account(AccountType.CASH, eur, "Euro Wallet");
        final Account brokerage = account(AccountType.INVEST, usd, "Brokerage");
        final Account income = account(AccountType.INCOME, usd, "Income");
        final Account salary = account(AccountType.INCOME, usd, "Salary");
        final Account dividends = account(AccountType.INCOME, usd, "Dividends");
        final Account expenses = account(AccountType.EXPENSE, usd, "Expenses");
        final Account groceries = account(AccountType.EXPENSE, usd, "Groceries");
        final Account fees = account(AccountType.EXPENSE, usd, "Fees");
        final Account liabilities = account(AccountType.LIABILITY, usd, "Liabilities");
        final Account creditCard = account(AccountType.CREDIT, usd, "Credit Card");

        addAccount(engine, engine.getRootAccount(), assets);
        addAccount(engine, assets, checking);
        addAccount(engine, assets, euroWallet);
        addAccount(engine, assets, brokerage);
        addAccount(engine, engine.getRootAccount(), income);
        addAccount(engine, income, salary);
        addAccount(engine, income, dividends);
        addAccount(engine, engine.getRootAccount(), expenses);
        addAccount(engine, expenses, groceries);
        addAccount(engine, expenses, fees);
        addAccount(engine, engine.getRootAccount(), liabilities);
        addAccount(engine, liabilities, creditCard);

        final SecurityNode security = new SecurityNode(usd);
        security.setSymbol("SYNTH");
        security.setDescription("Synthetic Index Fund");
        require(engine.addSecurity(security), "Could not add security");
        require(engine.addAccountSecurity(brokerage, security), "Could not link security");

        final Tag household = tag("household", "Synthetic household spending");
        final Tag reviewed = tag("reviewed", "Synthetic reviewed transaction");
        require(engine.addTag(household), "Could not add household tag");
        require(engine.addTag(reviewed), "Could not add reviewed tag");

        final Transaction opening = TransactionFactory.generateDoubleEntryTransaction(checking, salary,
                new BigDecimal("2500.00"), LocalDate.of(2020, 1, 10), "Synthetic salary", "Example Employer", "1001");
        opening.setReconciled(checking, ReconciledState.RECONCILED);
        opening.setTags(Set.of(reviewed));
        require(engine.addTransaction(opening), "Could not add salary transaction");

        final Transaction multiCurrency = TransactionFactory.generateDoubleEntryTransaction(checking, euroWallet,
                new BigDecimal("110.00"), new BigDecimal("100.00"), LocalDate.of(2020, 2, 20),
                "Synthetic currency transfer", "Example Exchange", "1002");
        require(engine.addTransaction(multiCurrency), "Could not add multi-currency transaction");

        final Transaction split = new Transaction();
        split.setDate(LocalDate.of(2020, 3, 15));
        split.setNumber("1003");
        split.setPayee("Example Market");
        split.setMemo("Synthetic split purchase");
        split.addTransactionEntry(TransactionFactory.createTransactionEntry(checking, groceries,
                new BigDecimal("60.00"), "Groceries portion", TransactionTag.BANK));
        split.addTransactionEntry(TransactionFactory.createTransactionEntry(checking, fees,
                new BigDecimal("10.00"), "Fee portion", TransactionTag.BANK));
        split.setTags(Set.of(household));
        require(engine.addTransaction(split), "Could not add split transaction");

        final InvestmentTransaction investment = TransactionFactory.generateAddXTransaction(brokerage, security,
                new BigDecimal("25.00"), new BigDecimal("4.0000"), LocalDate.of(2020, 4, 1),
                "Synthetic investment lot");
        require(engine.addTransaction(investment), "Could not add investment transaction");

        final Transaction attached = TransactionFactory.generateDoubleEntryTransaction(creditCard, groceries,
                new BigDecimal("42.50"), LocalDate.of(2020, 5, 5), "Synthetic receipt", "Example Store", "1004");
        attached.setAttachment("synthetic-receipt.txt");
        require(engine.addTransaction(attached), "Could not add attached transaction");

        final Path sourceAttachment = Files.createTempFile("jgnash-synthetic-receipt-", ".txt");
        try {
            Files.writeString(sourceAttachment, "Synthetic fixture receipt\nAmount: 42.50 USD\n");
            require(engine.addAttachment(sourceAttachment, true), "Could not add attachment");
            final Path storedAttachment = dataFile.getParent().resolve("attachments").resolve(sourceAttachment.getFileName());
            Files.move(storedAttachment, storedAttachment.resolveSibling("synthetic-receipt.txt"));
        } finally {
            Files.deleteIfExists(sourceAttachment);
        }

        final MonthlyReminder reminder = new MonthlyReminder();
        reminder.setAccount(checking);
        reminder.setStartDate(LocalDate.of(2020, 6, 1));
        reminder.setDescription("Synthetic monthly transfer");
        reminder.setNotes("No real person or institution");
        reminder.setTransaction(TransactionFactory.generateDoubleEntryTransaction(checking, income,
                new BigDecimal("5.00"), LocalDate.of(2020, 6, 1), "Reminder template", "Example", "R-1"));
        require(engine.addReminder(reminder), "Could not add reminder");

        final Budget budget = new Budget();
        budget.setName("Synthetic Household Budget");
        budget.setDescription("Generated exclusively for migration tests");
        require(engine.addBudget(budget), "Could not add budget");
    }

    private static Account account(final AccountType type, final CurrencyNode currency, final String name) {
        final Account account = new Account(type, currency);
        account.setName(name);
        return account;
    }

    private static Tag tag(final String name, final String description) {
        final Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }

    private static void addAccount(final Engine engine, final Account parent, final Account child) {
        require(engine.addAccount(parent, child), "Could not add account " + child.getName());
    }

    private static void truncate(final Path source, final Path destination) throws IOException {
        final byte[] content = Files.readAllBytes(source);
        final int truncatedLength = Math.max(1, Math.min(64, content.length / 3));
        Files.write(destination, java.util.Arrays.copyOf(content, truncatedLength));
    }

    private static void prepareDirectory(final Path directory) throws IOException {
        if (Files.exists(directory)) {
            final List<Path> paths = new ArrayList<>();
            try (var stream = Files.walk(directory)) {
                stream.sorted(Comparator.reverseOrder()).forEach(paths::add);
            }
            for (final Path path : paths) {
                Files.delete(path);
            }
        }
        Files.createDirectories(directory);
    }

    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class FixtureDefinition {
        private final String id;
        private final String fileName;
        private final DataStoreType type;
        private final char[] password;

        private FixtureDefinition(final String id, final String fileName, final DataStoreType type,
                                  final char[] password) {
            this.id = id;
            this.fileName = fileName;
            this.type = type;
            this.password = password;
        }
    }
}
