package academy.backend.market_pulse.history.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (бинарный, текстовый и т.д.); главное,
 * чтобы каждый {@link #append} создавал НОВЫЙ файл (не переписывал предыдущий) и
 * {@link #load(Path, String)} возвращал все сохранённые точки по тикеру в порядке возрастания
 * времени. Проверьте себя тестом {@code PriceHistoryStoreContractTest} (замените в нём эталонную
 * реализацию на свою).
 */
public class PriceHistoryStoreSkeleton implements PriceHistoryStore {

    @Override
    public Path append(Path directory, PricePoint point) {
        // TODO: записать точку в новый файл внутри directory — существующие файлы не трогать.
        //  Подсказка: имя файла на основе point.timestamp() даёт сортировку по имени = сортировку
        //  по времени.

        try {
            Files.createDirectories(directory); // на случай, если папки еще нет

            long epochMillis = point.timestamp().toEpochMilli();

            Path file = directory.resolve(point.ticker() + "_" + epochMillis + ".csv");

            int suffix = 1;
            while (Files.exists(file)) {
                file = directory.resolve(point.ticker() + "_" + epochMillis + "_" + suffix + ".csv");
                suffix++;
            }

            String line = point.ticker() + ";" + epochMillis + ";" + point.price().toPlainString();

            Files.writeString(file, line, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать точку истории цены в " + directory, e);
        }
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        // TODO: прочитать все точки по тикеру в порядке возрастания времени.
        //  Если для тикера ещё нет точек — вернуть пустой список, а не бросать исключение.

        if (Files.notExists(directory)) {
            return List.of();
        }

        List<PricePoint> points = new ArrayList<>();

        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> matchingFiles = stream
                    .filter(p -> p.getFileName().toString().startsWith(ticker + "_"))
                    .toList();

            for (Path file : matchingFiles) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.isBlank()) continue;

                    String[] parts = line.split(";", 3);
                    if (parts.length == 3) {
                        points.add(new PricePoint(
                                parts[0],
                                Instant.ofEpochMilli(Long.parseLong(parts[1])),
                                new BigDecimal(parts[2])
                        ));
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать историю из " + directory, e);
        }

        points.sort(Comparator.comparing(PricePoint::timestamp));

        return points;
    }
}
