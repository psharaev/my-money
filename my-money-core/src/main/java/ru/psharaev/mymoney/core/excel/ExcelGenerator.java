package ru.psharaev.mymoney.core.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.CategoryService;
import ru.psharaev.mymoney.core.FlowService;
import ru.psharaev.mymoney.core.TransactionService;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.entity.Category;
import ru.psharaev.mymoney.core.entity.Flow;
import ru.psharaev.mymoney.core.entity.Transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelGenerator {
    private static final Object CATEGORY_MONITOR = new Object();

    private final AccountService accountService;
    private final FlowService flowService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    private final Map<Long, String> categories = new ConcurrentHashMap<>();

    public InputStream generate(long userId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            generate(userId, workbook);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            workbook.write(stream);
            return new ByteArrayInputStream(stream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void generate(long userId, Workbook workbook) {
        generateAccounts(userId, workbook.createSheet("Accounts"));
        generateFlows(userId, workbook.createSheet("Flows"));
        generateTransactions(userId, workbook.createSheet("Transactions"));
    }

    private String decodeCategory(long categoryId) {
        String res = categories.get(categoryId);
        if (res != null) {
            return res;
        }

        synchronized (CATEGORY_MONITOR) {
            res = categories.get(categoryId);
            if (res != null) {
                return res;
            }

            Optional<Category> category = categoryService.findCategory(categoryId);
            if (category.isPresent()) {
                categories.put(categoryId, category.get().getName());
                return category.get().getName();
            }
        }

        return "unknown";
    }

    private void generateAccounts(long userId, Sheet accounts) {
        Row header = accounts.createRow(0);

        generateHeader(header, "id", "name", "currency");

        int rowNumber = 1;
        for (Account account : accountService.getAllAccounts(userId)) {
            Row row = accounts.createRow(rowNumber++);

            int colNumber = 0;
            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(account.getAccountId());

            row.createCell(colNumber++, CellType.STRING)
                    .setCellValue(account.getName());

            row.createCell(colNumber++, CellType.STRING)
                    .setCellValue(account.getCurrency().getCurrencyCode());
        }
    }

    private void generateFlows(long userId, Sheet flows) {
        Row header = flows.createRow(0);

        generateHeader(header, "id", "account_id", "amount", "time", "category", "description");

        int rowNumber = 1;
        for (Flow flow : flowService.getAllFlows(userId)) {
            Row row = flows.createRow(rowNumber++);

            int colNumber = 0;
            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getFlowId());

            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getAccountId());

            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getAmount().doubleValue());


            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(LocalDateTime.ofInstant(flow.getTime(), ZoneOffset.UTC));

            row.createCell(colNumber++, CellType.STRING)
                    .setCellValue(decodeCategory(flow.getCategoryId()));

            row.createCell(colNumber++, CellType.STRING)
                    .setCellValue(flow.getDescription());
        }
    }

    private void generateTransactions(long userId, Sheet transactions) {
        Row header = transactions.createRow(0);

        generateHeader(header, "id", "from_account_id", "to_account_id", "from_amount", "to_amount", "time", "category", "description");
        int rowNumber = 1;
        for (Transaction flow : transactionService.getAllTransactions(userId)) {
            Row row = transactions.createRow(rowNumber++);

            int colNumber = 0;
            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getTransactionId());

            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getFromAccountId());

            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getToAccountId());

            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getFromAmount().doubleValue());

            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(flow.getToAmount().doubleValue());

            row.createCell(colNumber++, CellType.NUMERIC)
                    .setCellValue(LocalDateTime.ofInstant(flow.getTime(), ZoneOffset.UTC));

            row.createCell(colNumber++, CellType.STRING)
                    .setCellValue(decodeCategory(flow.getCategoryId()));

            row.createCell(colNumber++, CellType.STRING)
                    .setCellValue(flow.getDescription());
        }
    }

    private static void generateHeader(Row header, String... names) {
        for (int i = 0; i < names.length; i++) {
            Cell cell = header.createCell(i, CellType.STRING);
            cell.setCellValue(names[i]);
        }
    }
}
