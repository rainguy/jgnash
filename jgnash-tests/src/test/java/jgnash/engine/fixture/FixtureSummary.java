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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jgnash.engine.Account;
import jgnash.engine.Engine;
import jgnash.engine.InvestmentTransaction;
import jgnash.engine.ReconciledState;
import jgnash.engine.Transaction;
import jgnash.engine.TransactionEntry;

/** Produces a stable, machine-readable semantic summary through public APIs. */
final class FixtureSummary {

    private FixtureSummary() {
    }

    static Map<String, String> summarize(final Engine engine, final Path dataFile, final String formatVersion)
            throws IOException {
        final Map<String, String> summary = new TreeMap<>();
        final List<Account> accounts = engine.getAccountList();
        final List<Transaction> transactions = engine.getTransactions();

        summary.put("accounts.count", Integer.toString(accounts.size()));
        summary.put("accounts.hierarchy", accounts.stream().map(Account::getPathName).sorted()
                .collect(Collectors.joining("|")));
        summary.put("attachments.count", Integer.toString(attachmentFiles(dataFile).size()));
        summary.put("attachments.files", summarizeAttachments(dataFile));
        summary.put("budgets.count", Integer.toString(engine.getBudgetList().size()));
        summary.put("commodities.currencies.count", Integer.toString(engine.getCurrencies().size()));
        summary.put("commodities.currencies.symbols", engine.getCurrencies().stream().map(node -> node.getSymbol())
                .sorted().collect(Collectors.joining("|")));
        summary.put("commodities.securities.count", Integer.toString(engine.getSecurities().size()));
        summary.put("commodities.securities.symbols", engine.getSecurities().stream().map(node -> node.getSymbol())
                .sorted().collect(Collectors.joining("|")));
        summary.put("dates.earliest", transactions.stream().map(Transaction::getLocalDate).min(LocalDate::compareTo)
                .map(LocalDate::toString).orElse("none"));
        summary.put("dates.latest", transactions.stream().map(Transaction::getLocalDate).max(LocalDate::compareTo)
                .map(LocalDate::toString).orElse("none"));
        summary.put("format.version", formatVersion);
        summary.put("reminders.count", Integer.toString(engine.getReminders().size()));
        summary.put("tags.count", Integer.toString(engine.getTags().size()));
        summary.put("transactions.attachments.count", Long.toString(transactions.stream()
                .filter(transaction -> transaction.getAttachment() != null).count()));
        summary.put("transactions.count", Integer.toString(transactions.size()));
        summary.put("transactions.entries.count", Integer.toString(transactions.stream()
                .mapToInt(transaction -> transaction.getTransactionEntries().size()).sum()));
        summary.put("transactions.investment.count", Long.toString(transactions.stream()
                .filter(InvestmentTransaction.class::isInstance).count()));
        summary.put("transactions.multicurrency.entries.count", Long.toString(transactions.stream()
                .flatMap(transaction -> transaction.getTransactionEntries().stream())
                .filter(TransactionEntry::isMultiCurrency).count()));
        summary.put("transactions.reconciled.count", Long.toString(transactions.stream()
                .filter(transaction -> transaction.getAccounts().stream()
                        .anyMatch(account -> transaction.getReconciled(account) == ReconciledState.RECONCILED)).count()));
        summary.put("transactions.split.count", Long.toString(transactions.stream()
                .filter(transaction -> transaction.getTransactionEntries().size() > 1).count()));

        final Map<String, BigDecimal> credits = new TreeMap<>();
        final Map<String, BigDecimal> debits = new TreeMap<>();
        transactions.stream().flatMap(transaction -> transaction.getTransactionEntries().stream()).forEach(entry -> {
            credits.merge(entry.getCreditAccount().getCurrencyNode().getSymbol(), entry.getCreditAmount(),
                    BigDecimal::add);
            debits.merge(entry.getDebitAccount().getCurrencyNode().getSymbol(), entry.getDebitAmount(),
                    BigDecimal::add);
        });
        credits.forEach((currency, total) -> summary.put("totals.credit." + currency, normalize(total)));
        debits.forEach((currency, total) -> summary.put("totals.debit." + currency, normalize(total)));

        return summary;
    }

    static String asProperties(final Map<String, String> summary) {
        return summary.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator(), "", System.lineSeparator()));
    }

    private static String summarizeAttachments(final Path dataFile) throws IOException {
        final List<String> summaries = new ArrayList<>();
        for (final Path attachment : attachmentFiles(dataFile)) {
            summaries.add(attachment.getFileName() + ":" + Files.size(attachment) + ":" + sha256(attachment));
        }
        return String.join("|", summaries);
    }

    private static List<Path> attachmentFiles(final Path dataFile) throws IOException {
        final Path directory = dataFile.getParent().resolve("attachments");
        if (Files.notExists(directory)) {
            return List.of();
        }
        try (var paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile).sorted().collect(Collectors.toList());
        }
    }

    private static String sha256(final Path path) throws IOException {
        try {
            return toHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        } catch (final NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by every Java implementation", exception);
        }
    }

    private static String normalize(final BigDecimal value) {
        final BigDecimal normalized = value.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0).toPlainString() : normalized.toPlainString();
    }

    private static String toHex(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (final byte value : bytes) {
            builder.append(String.format("%02x", value & 0xff));
        }
        return builder.toString();
    }
}
