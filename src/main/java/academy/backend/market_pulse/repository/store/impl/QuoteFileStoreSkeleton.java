package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (JSON, CSV, построчный текст); главное,
 * чтобы {@code load(save(quotes))} возвращал те же котировки. Проверьте себя тестом
 * {@code QuoteFileStoreContractTest} (замените в нём эталонную реализацию на свою).
 */
public class QuoteFileStoreSkeleton implements QuoteFileStore {

    @Override
    public void save(Path path, List<Quote> quotes) {
        // TODO: сохранить котировки в файл по указанному пути.
        //  Подсказка: на каждую котировку нужно сохранить тикер, тип и валюту инструмента (чтобы
        //  потом восстановить его через InstrumentFactories.create), а также цену и изменение за
        //  период — этого достаточно, чтобы собрать Quote обратно.

        List<String> quotesToString = new ArrayList<>();

        for (Quote q : quotes) {
            quotesToString.add(q.getInstrument().getType() + ";" +
            q.getInstrument().getTicker() + ";" +
            q.getInstrument().getCurrency().name() + ";" +
            q.getPrice().toPlainString() + ";" +
            q.getChangePercent().toPlainString() + ";" +
            q.getInstrument().getName());
        }
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, quotesToString, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить котировки в " + path, e);
        }
    }

    @Override
    public List<Quote> load(Path path) {
        // TODO: прочитать котировки из файла и восстановить при старте.
        //  Если файла ещё нет (первый запуск) — вернуть пустой список, а не бросать исключение.

        if (Files.notExists(path)) {
            return List.of();
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Quote> quotes = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                quotes.add(parseLine(line, i+1));
            }
            return quotes;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать котировки из " + path, e);
        }
    }

    private static Quote parseLine(String line, int lineNumber) {
        String[] parts = line.split(";", 6);
        if (parts.length < 6) {
            throw new IllegalStateException(
                    "Строка " + lineNumber + ": ожидалось 6 полей, получено " + parts.length);
        }

        Instrument instrument = InstrumentFactories.create(
                parts[0], parts[1], parts[5], Currency.valueOf(parts[2])
        );

        return new Quote(instrument, new BigDecimal(parts[3]), new BigDecimal(parts[4]));
    }
}
